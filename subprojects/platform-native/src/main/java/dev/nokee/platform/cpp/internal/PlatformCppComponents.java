package dev.nokee.platform.cpp.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, NativeComponentModule.class})
public interface PlatformCppComponents {
	DefaultCppApplicationExtensionFactory cppApplicationFactory();
	DefaultCppLibraryExtensionFactory cppLibraryFactory();

	@Component.Factory
	interface Factory {
		PlatformCppComponents create(@BindsInstance Project project);
	}
}
