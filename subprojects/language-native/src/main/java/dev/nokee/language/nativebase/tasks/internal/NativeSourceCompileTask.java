package dev.nokee.language.nativebase.tasks.internal;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

public interface NativeSourceCompileTask extends NativeSourceCompile {
	Property<NativeToolChain> getToolChain();
	SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
