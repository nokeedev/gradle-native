package dev.nokee.platform.nativebase;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import org.gradle.api.tasks.TaskProvider;

public interface ExecutableBinary extends NativeBinary {
	/**
	 * Returns a view of all the compile tasks that participate to compiling all the object files for this binary.
	 *
	 * @return a view of {@link NativeSourceCompile} tasks, never null.
	 */
	TaskView<? extends NativeSourceCompile> getCompileTasks();

	/**
	 * Returns a provider for the task that links the object files into this binary.
	 *
	 * @return a provider of {@link LinkExecutable} task, never null.
	 */
	TaskProvider<? extends LinkExecutable> getLinkTask();

	/**
	 * Returns whether or not this binary can be built in the current environment.
	 *
	 * @return {@code true} if this binary can be built in the current environment or {@code false} otherwise.
	 */
	boolean isBuildable();
}
