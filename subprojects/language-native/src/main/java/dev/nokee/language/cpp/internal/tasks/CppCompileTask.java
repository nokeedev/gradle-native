package dev.nokee.language.cpp.internal.tasks;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.cpp.tasks.CppCompile;

@CacheableTask
public abstract class CppCompileTask extends CppCompile implements NativeSourceCompile {
}
