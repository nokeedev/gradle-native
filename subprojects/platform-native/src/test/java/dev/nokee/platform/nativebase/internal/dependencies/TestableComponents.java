package dev.nokee.platform.nativebase.internal.dependencies;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInstantiator;
import org.gradle.api.Project;

@Component(modules = {PlatformNativeDependenciesModule.class})
public interface TestableComponents {
	NativeApplicationComponentDependenciesFactory applicationComponentDependenciesFactory();
	NativeLibraryComponentDependenciesFactory libraryComponentDependenciesFactory();
	NativeComponentDependenciesFactory componentDependenciesFactory();
	DependencyBucketInstantiator instantiator();

	@Component.Factory
	interface Factory {
		TestableComponents create(@BindsInstance Project project);
	}
}
