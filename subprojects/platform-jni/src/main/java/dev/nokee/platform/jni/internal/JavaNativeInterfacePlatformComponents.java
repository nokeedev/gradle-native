package dev.nokee.platform.jni.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.internal.ComponentModule;
import dev.nokee.platform.base.internal.ComponentSourcesInternal;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, ComponentModule.class})
public interface JavaNativeInterfacePlatformComponents {
	ComponentSourcesInternal sources();

	@Component.Factory
	interface Factory {
		JavaNativeInterfacePlatformComponents create(@BindsInstance Project project);
	}
}
