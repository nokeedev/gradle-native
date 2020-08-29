package dev.nokee.testing.xctest.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;
import org.gradle.api.Project;

@Component(modules = {PlatformNativeDependenciesModule.class})
public interface TestingXCTestComponents {
	UiTestXCTestTestSuiteComponentImplFactory uiTestFactory();
	UnitTestXCTestTestSuiteComponentImplFactory unitTestFactory();

	@Component.Factory
	interface Factory {
		TestingXCTestComponents create(@BindsInstance Project project);
	}
}
