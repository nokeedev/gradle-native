package dev.nokee.language.objectivec.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;

@CacheableTask
public abstract class ObjectiveCCompileTask extends ObjectiveCCompile implements NativeSourceCompileTask {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
