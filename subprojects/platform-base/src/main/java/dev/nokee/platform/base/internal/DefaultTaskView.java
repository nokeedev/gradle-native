package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.TaskView;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.List;

public abstract class DefaultTaskView<T extends Task> implements TaskView<T> {
	private final List<TaskProvider<? extends T>> delegate;

	@Inject
	public DefaultTaskView(List<TaskProvider<? extends T>> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(action);
		}
	}
}
