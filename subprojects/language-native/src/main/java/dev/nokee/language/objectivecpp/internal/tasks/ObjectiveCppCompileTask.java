package dev.nokee.language.objectivecpp.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile;

@CacheableTask
public abstract class ObjectiveCppCompileTask extends ObjectiveCppCompile implements NativeSourceCompileTask {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
