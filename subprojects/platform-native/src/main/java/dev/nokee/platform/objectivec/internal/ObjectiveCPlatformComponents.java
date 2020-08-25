package dev.nokee.platform.objectivec.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface ObjectiveCPlatformComponents {
	ObjectiveCApplicationComponentSources applicationSources();
	ObjectiveCLibraryComponentSources librarySources();

	@Component.Factory
	interface Factory {
		ObjectiveCPlatformComponents create(@BindsInstance Project project);
	}
}
