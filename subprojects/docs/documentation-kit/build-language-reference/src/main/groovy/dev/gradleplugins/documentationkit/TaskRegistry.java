package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.tasks.TaskName;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TaskRegistry {
	private final TaskContainer taskContainer;
	private final TaskNamingScheme namingScheme;

	public TaskRegistry(TaskContainer taskContainer) {
		this(taskContainer, TaskNamingScheme.identity());
	}

	public TaskRegistry(TaskContainer taskContainer, TaskNamingScheme namingScheme) {
		this.taskContainer = taskContainer;
		this.namingScheme = namingScheme;
	}

	public <T extends Task> TaskProvider<T> register(String name, Class<T> type) {
		return taskContainer.register(namingScheme.taskName(TaskName.of(name)), type, realizeNodeWhenGraphBuilt(type)::accept);
	}

	public <T extends Task> TaskProvider<T> register(String name, Class<T> type, Consumer<? super T> action) {
		return taskContainer.register(namingScheme.taskName(TaskName.of(name)), type, realizeNodeWhenGraphBuilt(type).andThen(action)::accept);
	}

	public <T extends Task> TaskProvider<T> register(TaskName name, Class<T> type) {
		return taskContainer.register(namingScheme.taskName(name), type, realizeNodeWhenGraphBuilt(type)::accept);
	}

	public <T extends Task> TaskProvider<T> register(TaskName name, Class<T> type, Consumer<? super T> action) {
		return taskContainer.register(namingScheme.taskName(name), type, realizeNodeWhenGraphBuilt(type).andThen(action)::accept);
	}

	private static <T extends Task> Consumer<T> realizeNodeWhenGraphBuilt(Class<T> type) {
		val node = ModelNodeContext.getCurrentModelNode();
		return task -> {
			task.dependsOn(alwaysReturnsEmptyList(node::realize));
		};
	}

	private static <T> Callable<List<T>> alwaysReturnsEmptyList(Supplier<?> supplier) {
		return () -> {
			supplier.get();
			return Collections.emptyList();
		};
	}
}
