package dev.nokee.platform.jni.internal.plugins;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.platform.jni.internal.DefaultJavaNativeInterfaceLibraryComponentDependenciesFactory;
import dev.nokee.platform.jni.internal.DefaultJavaNativeInterfaceNativeComponentDependenciesFactory;
import dev.nokee.platform.jni.internal.PlatformJniDependenciesModule;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;
import org.gradle.api.Project;

@Component(modules = {PlatformJniDependenciesModule.class, PlatformNativeDependenciesModule.class})
public interface PlatformJniComponents {
	DefaultJavaNativeInterfaceLibraryComponentDependenciesFactory libraryComponentDependenciesFactory();
	DefaultJavaNativeInterfaceNativeComponentDependenciesFactory nativeComponentDependenciesFactory();

	@Component.Factory
	interface Factory {
		PlatformJniComponents create(@BindsInstance Project project);
	}
}
