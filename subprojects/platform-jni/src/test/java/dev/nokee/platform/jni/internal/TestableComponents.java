package dev.nokee.platform.jni.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInstantiator;
import org.gradle.api.Project;

@Component(modules = {PlatformJniDependenciesModule.class})
public interface TestableComponents {
	DependencyBucketInstantiator instantiator();

	@Component.Factory
	interface Factory {
		TestableComponents create(@BindsInstance Project project);
	}
}
