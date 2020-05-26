package dev.nokee.runtime.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.runtime.base.internal.Dimension;
import dev.nokee.runtime.base.internal.DimensionType;
import org.gradle.api.Action;

public abstract class BaseNativeLibraryComponent extends BaseNativeComponent {
	public BaseNativeLibraryComponent(DefaultNativeLibraryDependencies dependencies, NamingScheme names) {
		super(dependencies, names);
	}

	@Override
	public DefaultNativeLibraryDependencies getDependencies() {
		return (DefaultNativeLibraryDependencies)super.getDependencies();
	}

	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		action.execute(getDependencies());
	}

	@Override
	protected Iterable<DimensionType> createDimensions() {
		return ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE, DefaultBinaryLinkage.DIMENSION_TYPE);
	}

	@Override
	protected Iterable<BuildVariant> createBuildVariants() {
		ImmutableList.Builder<BuildVariant> builder = ImmutableList.builder();
		for (BuildVariant variant : super.createBuildVariants()) {
			builder.add(DefaultBuildVariant.of(ImmutableList.<Dimension>builder().addAll(variant.getDimensions()).add(DefaultBinaryLinkage.SHARED).build()));
		}
		return builder.build();
	}
}
