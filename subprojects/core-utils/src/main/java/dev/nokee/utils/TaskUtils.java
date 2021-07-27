package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.EqualsAndHashCode;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ProjectLayout;
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
	 * @return an action that configures the task's group, never null
	 */
	public static ActionUtils.Action<Task> configureBuildGroup() {
		return new ConfigureGroupAction(LifecycleBasePlugin.BUILD_GROUP);
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
	public static ActionUtils.Action<Task> configureDescription(String format, Object... args) {
		return new ConfigureDescriptionAction(new StringFormatSupplier(format, args));
	}

	@EqualsAndHashCode
	private static final class ConfigureDescriptionAction implements ActionUtils.Action<Task> {
		private final Supplier<? extends String> descriptionSupplier;

		public ConfigureDescriptionAction(Supplier<? extends String> descriptionSupplier) {
			this.descriptionSupplier = requireNonNull(descriptionSupplier);
		}

		@Override
		public void execute(Task task) {
			task.setDescription(descriptionSupplier.get());
		}

		@Override
		public String toString() {
			return "TaskUtils.configureDescription(" + descriptionSupplier.get() + ")";
		}
	}
}
