package dev.nokee.language.c.internal.tasks;

import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CCompileTask extends org.gradle.language.c.tasks.CCompile implements NativeSourceCompileTask, CCompile {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
