package dev.nokee.platform.base.internal.tasks;

import dev.nokee.platform.base.DomainObjectProvider;
import lombok.ToString;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

@ToString
final class TaskDomainObjectProvider<I extends Task> implements DomainObjectProvider<I> {
	private final TaskIdentifier<I> identifier;
	private final TaskProvider<I> delegate;

	public TaskDomainObjectProvider(TaskIdentifier<I> identifier, TaskProvider<I> delegate) {
		this.identifier = identifier;
		this.delegate = delegate;
	}

	@Override
	public I get() {
		return delegate.get();
	}

	@Override
	public Class<I> getType() {
		return identifier.getType();
	}

	@Override
	public TaskIdentifier<I> getIdentity() {
		return identifier;
	}

	@Override
	public void configure(Action<? super I> action) {
		delegate.configure(action);
	}

	@Override
	public <S> Provider<S> map(Transformer<? extends S, ? super I> transformer) {
		return delegate.map(transformer);
	}

	@Override
	public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super I> transformer) {
		return delegate.flatMap(transformer);
	}
}
