package dev.nokee.testing.nativebase.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;
import org.gradle.api.Project;

@Component(modules = {PlatformNativeDependenciesModule.class})
public interface TestingNativeBaseComponents {
	DefaultNativeTestSuiteComponentFactory testSuiteComponentFactory();

	@Component.Factory
	interface Factory {
		TestingNativeBaseComponents create(@BindsInstance Project project);
	}
}
