package dev.nokee.platform.objectivecpp.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.nativebase.internal.NativeComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, NativeComponentModule.class})
public interface PlatformObjectiveCppComponents {
	DefaultObjectiveCppApplicationExtensionFactory objectiveCppApplicationFactory();
	DefaultObjectiveCppLibraryExtensionFactory objectiveCppLibraryFactory();

	@Component.Factory
	interface Factory {
		PlatformObjectiveCppComponents create(@BindsInstance Project project);
	}
}
