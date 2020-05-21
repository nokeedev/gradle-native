package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

public abstract class DefaultNativeComponentDependencies implements NativeComponentDependencies {
	@Override
	public void implementation(Object notation) {
		
	}

	@Override
	public void implementation(Object notation, Action<? super ExternalModuleDependency> action) {

	}

	@Override
	public void compileOnly(Object notation) {

	}

	@Override
	public void compileOnly(Object notation, Action<? super ExternalModuleDependency> action) {

	}

	@Override
	public void linkOnly(Object notation) {

	}

	@Override
	public void linkOnly(Object notation, Action<? super ExternalModuleDependency> action) {

	}

	@Override
	public void runtimeOnly(Object notation) {

	}

	@Override
	public void runtimeOnly(Object notation, Action<? super ExternalModuleDependency> action) {

	}
}
