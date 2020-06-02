package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import org.gradle.api.Action;

import javax.inject.Inject;

public abstract class DefaultNativeApplicationComponent extends BaseNativeComponent<DefaultNativeApplicationVariant> implements DependencyAwareComponent<NativeComponentDependencies> {
	private final DefaultNativeComponentDependencies dependencies;

	@Inject
	public DefaultNativeApplicationComponent(NamingScheme names) {
		super(names, DefaultNativeApplicationVariant.class);
		this.dependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, getNames());
		getDimensions().convention(ImmutableSet.of(DefaultOperatingSystemFamily.DIMENSION_TYPE, DefaultMachineArchitecture.DIMENSION_TYPE, DefaultBinaryLinkage.DIMENSION_TYPE));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(dependencies);
	}

	@Override
	protected DefaultNativeApplicationVariant createVariant(String name, BuildVariant buildVariant) {
		NamingScheme names = getNames().forBuildVariant(buildVariant, getBuildVariants().get());
		DefaultNativeComponentDependencies variantDependencies = getDependencies();
		if (getBuildVariants().get().size() > 1) {
			variantDependencies = getObjects().newInstance(DefaultNativeComponentDependencies.class, names);
			variantDependencies.extendsFrom(getDependencies());
		}

		DefaultNativeApplicationVariant result = getObjects().newInstance(DefaultNativeApplicationVariant.class, name, names, buildVariant, variantDependencies);
		return result;
	}
}
