package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.TaskView;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class DefaultTaskView<T extends Task> implements TaskView<T>, TaskDependency {
	private final List<TaskProvider<? extends T>> delegate;

	@Inject
	protected abstract ProviderFactory getProviders();

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

	@Override
	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(() -> delegate.stream().map(TaskProvider::get).collect(Collectors.toSet()));
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Set<? extends T>>() {
			@Override
			public List<? extends S> transform(Set<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.add(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(new Transformer<List<? extends S>, Set<? extends T>>() {
			@Override
			public List<? extends S> transform(Set<? extends T> elements) {
				ImmutableList.Builder<S> result = ImmutableList.builder();
				for (T element : elements) {
					result.addAll(mapper.transform(element));
				}
				return result.build();
			}
		});
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return delegate.stream().map(TaskProvider::get).collect(Collectors.toSet());
	}
}
