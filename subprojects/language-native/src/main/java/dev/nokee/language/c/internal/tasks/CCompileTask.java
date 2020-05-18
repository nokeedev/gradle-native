package dev.nokee.language.c.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.c.tasks.CCompile;

@CacheableTask
public abstract class CCompileTask extends CCompile implements NativeSourceCompileTask {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
