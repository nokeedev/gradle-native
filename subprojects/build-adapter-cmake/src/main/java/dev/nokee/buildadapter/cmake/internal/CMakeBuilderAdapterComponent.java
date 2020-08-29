package dev.nokee.buildadapter.cmake.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerFactory;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;
import org.gradle.api.Project;

@Component(modules = {PlatformNativeDependenciesModule.class})
public interface CMakeBuilderAdapterComponent {
	ComponentDependenciesContainerFactory dependenciesContainerFactory();

	@Component.Factory
	interface Factory {
		CMakeBuilderAdapterComponent create(@BindsInstance Project project);
	}
}
