package dev.nokee.platform.objectivec.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, NativeComponentModule.class})
public interface PlatformObjectiveCComponents {
	DefaultObjectiveCApplicationExtensionFactory objectiveCApplicationFactory();
	DefaultObjectiveCLibraryExtensionFactory objectiveCLibraryFactory();

	@Component.Factory
	interface Factory {
		PlatformObjectiveCComponents create(@BindsInstance Project project);
	}
}
