package dev.nokee.platform.objectivecpp.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface ObjectiveCppPlatformComponents {
	ObjectiveCppApplicationComponentSources applicationSources();
	ObjectiveCppLibraryComponentSources librarySources();

	@Component.Factory
	interface Factory {
		ObjectiveCppPlatformComponents create(@BindsInstance Project project);
	}
}
