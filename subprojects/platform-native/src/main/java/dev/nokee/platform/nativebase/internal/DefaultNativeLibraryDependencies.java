package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

public abstract class DefaultNativeLibraryDependencies extends DefaultNativeComponentDependencies implements NativeLibraryDependencies {
	@Override
	public void api(Object notation) {

	}

	@Override
	public void api(Object notation, Action<? super ExternalModuleDependency> action) {

	}
}
