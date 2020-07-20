package dev.nokee.utils;

import dev.nokee.utils.internal.DeleteDirectoriesTaskAction;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.io.File;
import java.util.Arrays;

/**
 * Utilities for Gradle {@link Task} {@link Action}.
 */
public class TaskActionUtils {
	/**
	 * Returns an action that will delete the specified directories.
	 * The directories can be of type {@link File}, {@link java.nio.file.Path}, {@link org.gradle.api.file.Directory} or any deferred types of previous types.
	 *
	 * @see DeferredUtils
	 * @param directories the directories to delete when executing the action.
	 * @return an action that deletes the specified directories when executed.
	 */
	public static Action<Task> deleteDirectories(Object... directories) {
		return deleteDirectories(Arrays.asList(directories));
	}

	/**
	 * Returns an action that will delete the specified directories.
	 * The directories can be of type {@link File}, {@link java.nio.file.Path}, {@link org.gradle.api.file.Directory} or any deferred types of previous types.
	 *
	 * @see DeferredUtils
	 * @param directories the directories to delete when executing the action.
	 * @return an action that deletes the specified directories when executed.
	 */
	public static Action<Task> deleteDirectories(Iterable<Object> directories) {
		return new DeleteDirectoriesTaskAction(directories);
	}
}
