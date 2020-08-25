package dev.nokee.platform.cpp.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface CppPlatformComponents {
	CppApplicationComponentSources applicationSources();
	CppLibraryComponentSources librarySources();

	@Component.Factory
	interface Factory {
		CppPlatformComponents create(@BindsInstance Project project);
	}
}
