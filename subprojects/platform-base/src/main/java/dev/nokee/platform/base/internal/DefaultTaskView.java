package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.TaskView;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DefaultTaskView<T extends Task> extends AbstractView<T> implements TaskView<T>, TaskDependency {
	private final List<TaskProvider<? extends T>> delegate;
	private final Realizable realizeTrigger;

	@Inject
	public DefaultTaskView(List<TaskProvider<? extends T>> delegate, Realizable realizeTrigger) {
		this.delegate = delegate;
		this.realizeTrigger = realizeTrigger;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(action);
		}
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(element -> {
				if (spec.isSatisfiedBy(element)) {
					action.execute(element);
				}
			});
		};
	}

	@Override
	public Set<? extends T> get() {
		realizeTrigger.realize();
		return delegate.stream().map(TaskProvider::get).collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return delegate.stream().map(TaskProvider::get).collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
