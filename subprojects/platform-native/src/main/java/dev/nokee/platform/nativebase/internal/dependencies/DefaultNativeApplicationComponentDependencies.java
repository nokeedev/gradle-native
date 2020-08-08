package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;

import javax.inject.Inject;

public class DefaultNativeApplicationComponentDependencies extends DefaultNativeComponentDependencies implements NativeApplicationComponentDependencies, ComponentDependencies {
	@Inject
	public DefaultNativeApplicationComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
	}
}
