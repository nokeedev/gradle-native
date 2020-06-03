package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.provider.ProviderInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
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
	private final Class<T> elementType;
	private final List<TaskProvider<? extends T>> delegate;
	private final Realizable realizeTrigger;

	@Inject
	public DefaultTaskView(Class<T> elementType, List<TaskProvider<? extends T>> delegate, Realizable realizeTrigger) {
		this.elementType = elementType;
		this.delegate = delegate;
		this.realizeTrigger = realizeTrigger;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for task view must not be null");
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(action);
		}
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(action != null, "configure each action for task view must not be null");
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(element -> {
				if (type.isAssignableFrom(element.getClass())) {
					action.execute(type.cast(element));
				}
			});
		}
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for task view must not be null");
		for (TaskProvider<? extends T> task : delegate) {
			task.configure(element -> {
				if (spec.isSatisfiedBy(element)) {
					action.execute(element);
				}
			});
		}
	}

	@Override
	public <S extends T> TaskView<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "task view subview type must not be null");
		if (elementType.equals(type)) {
			return Cast.uncheckedCast("view types are the same", this);
		}
		return Cast.uncheckedCast("of type erasure", getObjects().newInstance(DefaultTaskView.class, type, delegate.stream().filter(it -> type.isAssignableFrom(typeOf(it))).collect(Collectors.toList()), realizeTrigger));
	}

	// We cheat a bit here and cast to ProviderInternal to get the type.
	// For TaskProvider, the type is never null.
	private static Class<?> typeOf(Provider<?> provider) {
		return ((ProviderInternal<?>)provider).getType();
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

	@Override
	protected String getDisplayName() {
		return "task view";
	}
}
