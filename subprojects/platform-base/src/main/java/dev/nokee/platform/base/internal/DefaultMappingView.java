package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.View;
import dev.nokee.utils.Cast;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DefaultMappingView<T, I> implements View<T> {
	private final View<I> delegate;
	private final Function<I, View<T>> mapper;

	public DefaultMappingView(View<I> delegate, Function<I, View<T>> mapper) {
		this.delegate = delegate;
		this.mapper = mapper;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(it -> mapper.apply(it).configureEach(action));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(it -> mapper.apply(it).configureEach(type, action));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(it -> mapper.apply(it).configureEach(spec, action));
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return new DefaultFilteringView<>(Cast.uncheckedCast("", this), it -> type.isAssignableFrom(it.getClass()));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return delegate.flatMap(it -> mapper.apply(it).getElements().get()).map(LinkedHashSet::new);
	}

	@Override
	public Set<? extends T> get() {
		return getElements().get();
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return delegate.flatMap(it -> this.mapper.apply(it).map(mapper).get());
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return delegate.flatMap(it -> this.mapper.apply(it).flatMap(mapper).get());
	}

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return delegate.flatMap(it -> this.mapper.apply(it).filter(spec).get());
	}
}
