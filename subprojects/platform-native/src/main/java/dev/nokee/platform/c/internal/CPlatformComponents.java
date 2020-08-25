package dev.nokee.platform.c.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface CPlatformComponents {
	CApplicationComponentSources applicationSources();
	CLibraryComponentSources librarySources();

	@Component.Factory
	interface Factory {
		CPlatformComponents create(@BindsInstance Project project);
	}
}
