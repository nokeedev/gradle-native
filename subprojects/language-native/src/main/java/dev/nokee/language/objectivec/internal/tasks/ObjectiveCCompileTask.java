package dev.nokee.language.objectivec.internal.tasks;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;

@CacheableTask
public abstract class ObjectiveCCompileTask extends ObjectiveCCompile implements NativeSourceCompile {
}
