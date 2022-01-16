/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

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

	/**
	 * Returns the temporary task directory as a directory provider.
	 * Calling {@link Task#getTemporaryDir()} creates the directory immediately.
	 * To avoid unnecessary directory creation during the configuration phase, we can use this method.
	 *
	 * @param task  the task to return the temporary task directory, must not be null
	 * @return the task's temporary directory as {@link Provider}, never null
	 */
	public static Provider<Directory> temporaryTaskDirectory(Task task) {
		return task.getProject().getLayout().getBuildDirectory().dir("tmp/" + task.getName());
	}

	/**
	 * Returns an action that will configure {@link Task#dependsOn(Object...)}.
	 *
	 * @param path a task dependency path as describe by {@link Task#dependsOn(Object...)}.
	 * @param paths an array of task dependency path as describe by {@link Task#dependsOn(Object...)}.
	 * @return an action that configures the task's dependencies.
	 */
	public static ActionUtils.Action<Task> configureDependsOn(Object path, Object... paths) {
		// At least one dependency must be specified.
		return new ConfigureDependsOnAction(ImmutableList.builder().add(path).add(paths).build());
	}

	@EqualsAndHashCode
	private static final class ConfigureDependsOnAction implements ActionUtils.Action<Task> {
		private final List<Object> paths;

		public ConfigureDependsOnAction(List<Object> paths) {
			this.paths = paths;
		}

		@Override
		public void execute(Task task) {
			task.dependsOn(Iterables.toArray(paths, Object.class));
		}

		@Override
		public String toString() {
			return "TaskUtils.configureDependsOn(" + paths.stream().map(Objects::toString).collect(joining(", ")) + ")";
		}
	}

	/**
	 * Returns an action that configures the task group to the specified group.
	 * The action uses {@link Task#setGroup(String)} to configure the group.
	 *
	 * @param group  a task group string.
	 * @return an action that configures the task's group, never null
	 */
	public static ActionUtils.Action<Task> configureGroup(String group) {
		return new ConfigureGroupAction(group);
	}

	/**
	 * Returns an action that configures the task group as {@literal build} group.
	 * Short hand version of {@code TaskUtils.configureGroup("group")}.
	 *
	 * @return an action that configures the task's group to {@literal build}, never null
	 */
	public static ActionUtils.Action<Task> configureBuildGroup() {
		return new ConfigureGroupAction(LifecycleBasePlugin.BUILD_GROUP);
	}

	/**
	 * Returns an action that configures the task group as {@literal verification} group.
	 * Show hand version of {@code TaskUtils.configureGroup("verification")}.
	 *
	 * @return an action that configures the task's group to {@literal verification}, never null
	 */
	public static ActionUtils.Action<Task> configureVerificationGroup() {
		return new ConfigureGroupAction(LifecycleBasePlugin.VERIFICATION_GROUP);
	}

	@EqualsAndHashCode
	private static final class ConfigureGroupAction implements ActionUtils.Action<Task> {
		private final String group;

		public ConfigureGroupAction(String group) {
			this.group = requireNonNull(group);
		}

		@Override
		public void execute(Task task) {
			task.setGroup(group);
		}

		@Override
		public String toString() {
			return "TaskUtils.configureGroup(" + group + ")";
		}
	}

	/**
	 * Returns an action that configures the {@link Task} description as formatted string.
	 *
	 * @param format  string format, must not be null
	 * @param args  string format arguments, must not be null
	 * @return an action that configures the {@link Task}'s description, never null
	 */
	// We use a generic task type to help with chaining
	//  TaskUtils.<SomeTask>configureDescription(...).andThen(specificTaskConfiguration())
	public static <T extends Task> ActionUtils.Action<T> configureDescription(String format, Object... args) {
		return new ConfigureDescriptionAction<>(new StringFormatSupplier(format, args));
	}

	/** @see #configureDescription(String, Object...) */
	@EqualsAndHashCode
	private static final class ConfigureDescriptionAction<T extends Task> implements ActionUtils.Action<T> {
		private final Supplier<? extends String> descriptionSupplier;

		public ConfigureDescriptionAction(Supplier<? extends String> descriptionSupplier) {
			this.descriptionSupplier = requireNonNull(descriptionSupplier);
		}

		@Override
		public void execute(T task) {
			task.setDescription(descriptionSupplier.get());
		}

		@Override
		public String toString() {
			return "TaskUtils.configureDescription(" + descriptionSupplier.get() + ")";
		}
	}

	/**
	 * Adds the specified action to the target task's do first action list.
	 *
	 * @param action  the do first action to add, must not be null
	 * @param <SELF>  the target task type
	 * @return an action that adds the specified action to the task's do first action list, never null
	 */
	public static <SELF extends Task> ActionUtils.Action<SELF> configureDoFirst(Action<? super SELF> action) {
		requireNonNull(action);
		Preconditions.checkArgument(!isJavaLambda(action)); // lambda's breaks task caching, so we just disallow them
		return new ConfigureDoFirstAction<>(action);
	}

	/** @see #configureDoFirst(Action) */
	@EqualsAndHashCode
	private static final class ConfigureDoFirstAction<T extends Task> implements ActionUtils.Action<T> {
		private final Action<? super T> action;

		private ConfigureDoFirstAction(Action<? super T> action) {
			this.action = requireNonNull(action);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void execute(T task) {
			if (action instanceof Named) {
				task.doFirst(((Named) action).getName(), (Action<? super Task>) action);
			} else {
				task.doFirst((Action<? super Task>) action);
			}
		}

		@Override
		public String toString() {
			return "TaskUtils.configureDoFirst(" + action + ")";
		}
	}

	private static boolean isJavaLambda(Object obj) {
		return obj.getClass().getSimpleName().contains("$$Lambda$");
	}
}
