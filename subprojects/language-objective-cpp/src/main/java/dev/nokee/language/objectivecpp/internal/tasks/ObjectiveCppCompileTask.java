package dev.nokee.language.objectivecpp.internal.tasks;

import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class ObjectiveCppCompileTask extends org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile implements NativeSourceCompileTask, ObjectiveCppCompile {
	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
