package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public class BaseView<T> implements View<T> {
	private final View<T> delegate;

	protected BaseView(View<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return delegate.withType(type);
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return delegate.getElements();
	}

	@Override
	public Set<? extends T> get() {
		return delegate.get();
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
