package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.experimental.Delegate;

final class NativeApplicationComponentDependenciesImpl extends BaseComponentDependenciesContainer implements NativeApplicationComponentDependencies, NativeApplicationComponentDependenciesInternal, ComponentDependencies {
	@Delegate private final NativeComponentDependencies delegate;

	public NativeApplicationComponentDependenciesImpl(ComponentDependenciesContainer delegate) {
		super(delegate);
		this.delegate = new NativeComponentDependenciesImpl(delegate);
	}
}
