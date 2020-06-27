package dev.nokee.platform.nativebase;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;

public interface NativeBinary extends Binary {
	/**
	 * Returns a view of all the compile tasks that participate to compiling all the object files for this binary.
	 *
	 * @return a view of {@link SourceCompile} tasks, never null.
	 */
	TaskView<SourceCompile> getCompileTasks();

	/**
	 * Returns whether or not this binary can be built in the current environment.
	 *
	 * @return {@code true} if this binary can be built in the current environment or {@code false} otherwise.
	 */
	boolean isBuildable();
}
