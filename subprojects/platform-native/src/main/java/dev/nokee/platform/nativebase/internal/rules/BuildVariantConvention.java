package dev.nokee.platform.nativebase.internal.rules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.*;
import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BuildVariantConvention implements Callable<Iterable<BuildVariantInternal>> {
	private final Object extension;
	private final Object component;
	private final Supplier<Set<DimensionType>> dimensionSupplier;

	public BuildVariantConvention(Object extension, Object component, Supplier<Set<DimensionType>> dimensionSupplier) {
		this.extension = extension;
		this.component = component;
		this.dimensionSupplier = dimensionSupplier;
	}

	@Override
	public Iterable<BuildVariantInternal> call() throws Exception {
		val buildVariantBuilder = ImmutableList.<BuildVariantInternal>builder();

		val builder = ImmutableList.<Set<Dimension>>builder();

		// Handle operating system family and machine architecture dimension
		if (extension instanceof TargetMachineAwareComponent) {
			builder.add(((TargetMachineAwareComponent) extension).getTargetMachines().get().stream().map(it -> new TargetMachineDimension((DefaultTargetMachine)it)).collect(ImmutableSet.toImmutableSet()));
		}

		// Handle linkage dimension
		if (extension instanceof TargetLinkageAwareComponent) {
			Set<TargetLinkage> targetLinkages = ((TargetLinkageAwareComponent) extension).getTargetLinkages().get();
			builder.add(targetLinkages.stream().map(DefaultBinaryLinkage.class::cast).collect(Collectors.toSet()));
		} else if (component instanceof DefaultNativeApplicationComponent) {
			builder.add(ImmutableSet.of(DefaultBinaryLinkage.EXECUTABLE));
		} else if (component instanceof DefaultNativeLibraryComponent) {
			builder.add(ImmutableSet.of(DefaultBinaryLinkage.SHARED));
		}

		// Handle build type dimension
		if (extension instanceof TargetBuildTypeAwareComponent) {
			Set<TargetBuildType> targetBuildTypes = ((TargetBuildTypeAwareComponent) extension).getTargetBuildTypes().get();
			builder.add(targetBuildTypes.stream().map(BaseTargetBuildType.class::cast).collect(ImmutableSet.toImmutableSet()));
		} else {
			builder.add(ImmutableSet.of(DefaultTargetBuildTypeFactory.DEFAULT));
		}

		Sets.cartesianProduct(builder.build()).forEach(dimensions -> {
			ImmutableList.Builder<Dimension> dimensionBuilder = ImmutableList.builder();
			dimensions.forEach(dimension -> {
				if (dimension instanceof TargetMachineDimension) {
					dimensionBuilder.add(((TargetMachineDimension) dimension).targetMachine.getOperatingSystemFamily());
					dimensionBuilder.add(((TargetMachineDimension) dimension).targetMachine.getArchitecture());
				} else {
					dimensionBuilder.add(dimension);
				}
			});
			buildVariantBuilder.add(DefaultBuildVariant.of(sort(dimensionBuilder.build())));
		});
		return buildVariantBuilder.build();
	}

	private Iterable<Dimension> sort(Collection<Dimension> dimensionsToOrder) {
		val result = ImmutableList.<Dimension>builder();
		for (val type : dimensionSupplier.get()) {
			result.add(dimensionsToOrder.stream().filter(it -> it.getType().equals(type)).findFirst().orElseThrow(() -> new IllegalArgumentException("Missing dimension " + type)));
		}
		return result.build();
	}

	@RequiredArgsConstructor
	private static final class TargetMachineDimension implements Dimension {
		private static final DimensionType<?> DIMENSION_TYPE = new DefaultDimensionType<>(TargetMachineDimension.class);
		public final DefaultTargetMachine targetMachine;

		@Override
		public DimensionType getType() {
			return DIMENSION_TYPE;
		}
	}
}
