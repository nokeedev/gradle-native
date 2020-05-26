package dev.nokee.runtime.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import org.gradle.api.Action;

import java.util.Set;

public abstract class BaseNativeApplicationComponent extends BaseNativeComponent {
	public BaseNativeApplicationComponent(DefaultNativeComponentDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
	}

	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	protected Set<DimensionType> createDimensions() {
		return ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE);
	}

	@Override
	protected Iterable<BuildVariant> createBuildVariants() {
		ImmutableList.Builder<BuildVariant> builder = ImmutableList.builder();
		for (BuildVariant variant : super.createBuildVariants()) {
			builder.add(DefaultBuildVariant.of(ImmutableList.<Dimension>builder().addAll(variant.getDimensions()).add(DefaultBinaryLinkage.EXECUTABLE).build()));
		}
		return builder.build();
	}
}
