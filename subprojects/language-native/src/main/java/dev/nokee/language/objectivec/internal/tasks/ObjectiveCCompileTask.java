package dev.nokee.language.objectivec.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class ObjectiveCCompileTask extends org.gradle.language.objectivec.tasks.ObjectiveCCompile implements NativeSourceCompileTask, ObjectiveCCompile {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
