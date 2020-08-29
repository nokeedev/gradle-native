package dev.nokee.platform.ios.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;
import org.gradle.api.Project;

@Component(modules = {PlatformNativeDependenciesModule.class})
public interface PlatformIosComponents {
	DefaultIosApplicationComponentFactory applicationFactory();

	@Component.Factory
	interface Factory {
		PlatformIosComponents create(@BindsInstance Project project);
	}
}
