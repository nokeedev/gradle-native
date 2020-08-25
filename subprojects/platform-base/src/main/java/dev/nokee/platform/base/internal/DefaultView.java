package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.DomainObjectCollection;
import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public class DefaultView<T> implements View<T> {
	private final DomainObjectCollection<T> delegate;

	public DefaultView(DomainObjectCollection<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		delegate.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		delegate.configureEach(type, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "variant view subview type must not be null");
		return delegate.withType(type);
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return delegate.getElements().map(ImmutableSet::copyOf);
	}

	@Override
	public Set<? extends T> get() {
		return ImmutableSet.copyOf(delegate.getElements().get());
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return delegate.map(mapper);
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return delegate.flatMap(mapper);
	}

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return delegate.filter(spec);
	}
}
