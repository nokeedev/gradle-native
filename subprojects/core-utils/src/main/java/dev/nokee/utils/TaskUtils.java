package dev.nokee.utils;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;

/**
 * Utilities for Gradle {@link Task} instances.
 */
public final class TaskUtils {
	/**
	 * Returns the relative path from {@link ProjectLayout#getBuildDirectory()} (or {@link Project#getBuildDir()}) to the specified task temporary directory.
	 * Calling {@link Task#getTemporaryDir()} creates the directory immediately.
	 * To avoid unnecessary directory creation during the configuration phase, we can use this method together with {@link ProjectLayout#getBuildDirectory()} to create a side-effect free path to the task's temporary directory.
	 *
	 * @param task the task to return the temporary directory path.
	 * @return the task's temporary directory relative path from the build directory.
	 */
	public static String temporaryDirectoryPath(Task task) {
		return "tmp/" + task.getName();
	}
}
