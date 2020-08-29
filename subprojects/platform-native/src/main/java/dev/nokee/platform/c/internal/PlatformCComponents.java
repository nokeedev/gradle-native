package dev.nokee.platform.c.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, NativeComponentModule.class})
public interface PlatformCComponents {
	DefaultCApplicationExtensionFactory cApplicationFactory();
	DefaultCLibraryExtensionFactory cLibraryFactory();

	@Component.Factory
	interface Factory {
		PlatformCComponents create(@BindsInstance Project project);
	}
}
