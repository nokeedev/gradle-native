package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

final class NativeLibraryComponentDependenciesImpl extends BaseComponentDependenciesContainer implements NativeLibraryComponentDependencies, NativeLibraryComponentDependenciesInternal, ComponentDependencies {
	@Delegate private final NativeComponentDependenciesInternal delegate;
	@Getter private final DeclarableDependencyBucket api;

	public NativeLibraryComponentDependenciesImpl(ComponentDependenciesContainer delegate) {
		super(delegate);
		this.delegate = new NativeComponentDependenciesImpl(delegate);
		this.api = register(DependencyBucketName.of("api"), DeclarableMacOsFrameworkAwareDependencies.class, this::configureApiBucket);
	}

	private void configureApiBucket(DeclarableMacOsFrameworkAwareDependencies bucket) {
		// Configure this here to simplify testing, it ends up being the same
		getImplementation().extendsFrom(bucket);
	}

	@Override
	public void api(Object notation) {
		getApi().addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		getApi().addDependency(notation, action);
	}
}
