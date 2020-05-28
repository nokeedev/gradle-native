package dev.nokee.language.cpp.internal.tasks;

import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CppCompileTask extends org.gradle.language.cpp.tasks.CppCompile implements NativeSourceCompileTask, CppCompile {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
