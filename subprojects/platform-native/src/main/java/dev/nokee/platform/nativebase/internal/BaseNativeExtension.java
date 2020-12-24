package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.base.internal.DefaultDimensionType;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseNativeExtension<T extends BaseNativeComponent<?>> implements ModelNodeAware {
	@Getter private final T component;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;

	public BaseNativeExtension(T component, ObjectFactory objects, ProviderFactory providers, ProjectLayout layout) {
		this.component = component;
		this.objects = objects;
		this.providers = providers;
		this.layout = layout;

		component.getBuildVariants().convention(getProviders().provider(this::createBuildVariants));
		component.getBuildVariants().finalizeValueOnRead();
		component.getBuildVariants().disallowChanges(); // Let's disallow changing them for now.

		component.getDimensions().disallowChanges(); // Let's disallow changing them for now.
	}

	protected Transformer<Iterable<FileSystemLocation>, Set<FileSystemLocation>> toIfEmpty(String path) {
		return sources -> {
			if (sources.isEmpty()) {
				return ImmutableList.of(getLayout().getProjectDirectory().file(path));
			}
			return sources;
		};
	}

	@Override
	public ModelNode getNode() {
		return ModelNodes.of(component);
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

	protected Iterable<BuildVariantInternal> createBuildVariants() {
		ImmutableList.Builder<BuildVariantInternal> buildVariantBuilder = ImmutableList.builder();

		ImmutableList.Builder<Set<Dimension>> builder = ImmutableList.builder();

		// Handle operating system family and machine architecture dimension
		if (this instanceof TargetMachineAwareComponent) {
			builder.add(((TargetMachineAwareComponent) this).getTargetMachines().get().stream().map(it -> new TargetMachineDimension((DefaultTargetMachine)it)).collect(ImmutableSet.toImmutableSet()));
		}

		// Handle linkage dimension
		if (this instanceof TargetLinkageAwareComponent) {
			Set<TargetLinkage> targetLinkages = ((TargetLinkageAwareComponent) this).getTargetLinkages().get();
			builder.add(targetLinkages.stream().map(DefaultBinaryLinkage.class::cast).collect(Collectors.toSet()));
		} else if (component instanceof DefaultNativeApplicationComponent) {
			builder.add(ImmutableSet.of(DefaultBinaryLinkage.EXECUTABLE));
		} else if (component instanceof DefaultNativeLibraryComponent) {
			builder.add(ImmutableSet.of(DefaultBinaryLinkage.SHARED));
		}

		// Handle build type dimension
		if (this instanceof TargetBuildTypeAwareComponent) {
			Set<TargetBuildType> targetBuildTypes = ((TargetBuildTypeAwareComponent) this).getTargetBuildTypes().get();
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
		for (val type : component.getDimensions().get()) {
			result.add(dimensionsToOrder.stream().filter(it -> it.getType().equals(type)).findFirst().orElseThrow(() -> new IllegalArgumentException("Missing dimension")));
		}
		return result.build();
	}

	public DefaultTargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}

	public DefaultTargetLinkageFactory getLinkages() {
		return DefaultTargetLinkageFactory.INSTANCE;
	}

	public DefaultTargetBuildTypeFactory getBuildTypes() {
		return DefaultTargetBuildTypeFactory.INSTANCE;
	}

	public BinaryView<Binary> getBinaries() {
		return component.getBinaries();
	}
}
