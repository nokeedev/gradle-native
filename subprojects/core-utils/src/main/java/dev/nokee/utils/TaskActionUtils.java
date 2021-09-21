/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.utils;

import dev.nokee.utils.internal.AssertingTaskAction;
import dev.nokee.utils.internal.DeleteDirectoriesTaskAction;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.io.File;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Utilities for Gradle {@link Task} {@link Action}.
 */
public final class TaskActionUtils {
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

	/**
	 * Assert the supplied expression is true as a Task action.
	 * The expression will be evaluated only once when the task action will be executed.
	 *
	 * @param expression expression to assert true
	 * @param errorMessage error message to include in the exception
	 * @return an action that assert the specified expression is true or else it will throw an {@link IllegalArgumentException}.
	 * @throws IllegalArgumentException if the expression is {@code false}.
	 */
	public static Action<Task> assertTrue(Supplier<Boolean> expression, Object errorMessage) {
		return new AssertingTaskAction(expression, errorMessage);
	}
}
