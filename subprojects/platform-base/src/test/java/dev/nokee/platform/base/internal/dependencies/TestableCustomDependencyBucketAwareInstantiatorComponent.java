package dev.nokee.platform.base.internal.dependencies;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import org.gradle.api.Project;

@Component(modules = {CustomDependencyBucketModule.class, PlatformBaseDependenciesModule.class, GradleModule.class})
public interface TestableCustomDependencyBucketAwareInstantiatorComponent {
	DependencyBucketInstantiator instantiator();

	@Component.Factory
	interface Factory {
		TestableCustomDependencyBucketAwareInstantiatorComponent create(@BindsInstance Project project);
	}
}
