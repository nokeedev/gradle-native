package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;

import javax.inject.Inject;

public abstract class DefaultNativeComponentDependencies extends AbstractNativeComponentDependencies implements NativeComponentDependencies {
	@Inject
	public DefaultNativeComponentDependencies(NamingScheme names) {
		super(names);
	}

	public DefaultNativeComponentDependencies extendsFrom(DefaultNativeComponentDependencies dependencies) {
		getImplementationDependencies().extendsFrom(dependencies.getImplementationDependencies());
		getCompileOnlyDependencies().extendsFrom(dependencies.getCompileOnlyDependencies());
		getLinkOnlyDependencies().extendsFrom(dependencies.getLinkOnlyDependencies());
		getRuntimeOnlyDependencies().extendsFrom(dependencies.getRuntimeOnlyDependencies());
		return this;
	}
}
