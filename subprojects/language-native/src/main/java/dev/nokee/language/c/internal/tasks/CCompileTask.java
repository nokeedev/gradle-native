package dev.nokee.language.c.internal.tasks;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.c.tasks.CCompile;

@CacheableTask
public abstract class CCompileTask extends CCompile implements NativeSourceCompile {
}
