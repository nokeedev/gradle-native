package dev.nokee.language.cpp.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.cpp.tasks.CppCompile;

@CacheableTask
public abstract class CppCompileTask extends CppCompile implements NativeSourceCompileTask {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
