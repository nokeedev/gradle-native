package dev.nokee.language.objectivecpp.internal.tasks;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile;

@CacheableTask
public abstract class ObjectiveCppCompileTask extends ObjectiveCppCompile implements NativeSourceCompile {
}
