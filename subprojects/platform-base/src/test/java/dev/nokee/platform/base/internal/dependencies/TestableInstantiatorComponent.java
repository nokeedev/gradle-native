package dev.nokee.platform.base.internal.dependencies;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import org.gradle.api.Project;

@Component(modules = {PlatformBaseDependenciesModule.class, GradleModule.class})
public interface TestableInstantiatorComponent {
	DependencyBucketInstantiator instantiator();

	@Component.Factory
	interface Factory {
		TestableInstantiatorComponent create(@BindsInstance Project project);
	}
}
