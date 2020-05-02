package dev.nokee.language.nativebase.tasks.internal;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

public abstract class NativeSourceCompileTask extends DefaultTask implements NativeSourceCompile {
	public abstract Property<NativeToolChain> getToolChain();
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
