package dev.nokee.platform.nativebase;

import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import org.gradle.api.tasks.TaskProvider;

/**
 * A shared library built from 1 or more native language.
 *
 * @since 0.3
 */
public interface SharedLibraryBinary extends Binary {
	/**
	 * Returns a view of all the compile tasks that participate to compiling all the object files for this binary.
	 *
	 * @return a view of {@link NativeSourceCompile} tasks, never null.
	 */
	TaskView<? extends NativeSourceCompile> getCompileTasks();

	/**
	 * Returns a provider for the task that links the object files into this binary.
	 *
	 * @return a provider of {@link LinkSharedLibrary} task, never null.
	 */
	TaskProvider<? extends LinkSharedLibrary> getLinkTask();
}
