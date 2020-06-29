package dev.nokee.platform.nativebase;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import org.gradle.api.tasks.TaskProvider;

/**
 * A executable built from 1 or more native language.
 *
 * @since 0.4
 */
public interface ExecutableBinary extends NativeBinary {
	/**
	 * Returns a view of all the compile tasks that participate to compiling all the object files for this binary.
	 *
	 * @return a view of {@link SourceCompile} tasks, never null.
	 */
	TaskView<SourceCompile> getCompileTasks();

	/**
	 * Returns a provider for the task that links the object files into this binary.
	 *
	 * @return a provider of {@link LinkExecutable} task, never null.
	 */
	TaskProvider<LinkExecutable> getLinkTask();

	/**
	 * Returns whether or not this binary can be built in the current environment.
	 *
	 * @return {@code true} if this binary can be built in the current environment or {@code false} otherwise.
	 */
	boolean isBuildable();
}
