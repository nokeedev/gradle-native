package dev.nokee.platform.swift.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface SwiftPlatformComponents {
	SwiftComponentSources sources();

	@Component.Factory
	interface Factory {
		SwiftPlatformComponents create(@BindsInstance Project project);
	}
}
