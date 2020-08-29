package dev.nokee.platform.swift.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, NativeComponentModule.class})
public interface PlatformSwiftComponents {
	DefaultSwiftApplicationExtensionFactory swiftApplicationFactory();
	DefaultSwiftLibraryExtensionFactory swiftLibraryFactory();

	@Component.Factory
	interface Factory {
		PlatformSwiftComponents create(@BindsInstance Project project);
	}
}
