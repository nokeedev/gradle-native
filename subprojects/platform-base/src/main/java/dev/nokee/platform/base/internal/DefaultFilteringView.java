package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.platform.base.View;
import dev.nokee.utils.Cast;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultFilteringView<T> implements View<T> {
	private final View<T> delegate;
	private final Spec<? super T> spec;

	public DefaultFilteringView(View<T> delegate, Spec<? super T> spec) {
		this.delegate = delegate;
		this.spec = spec;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(new Action<T>() {
			@Override
			public void execute(T element) {
				if (spec.isSatisfiedBy(element) && type.isAssignableFrom(element.getClass())) {
					action.execute(type.cast(element));
				}
			}
		});
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(Specs.intersect(this.spec, spec), action);
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return new DefaultFilteringView<S>(Cast.uncheckedCast("", delegate), Specs.intersect(spec, it -> type.isAssignableFrom(it.getClass())));
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return delegate.filter(spec).map(LinkedHashSet::new);
	}

	@Override
	public Set<? extends T> get() {
		return getElements().get();
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return delegate.flatMap(it -> {
			if (spec.isSatisfiedBy(it)) {
				return ImmutableList.of(mapper.transform(it));
			}
			return ImmutableList.of();
		});
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return delegate.flatMap(it -> {
			if (spec.isSatisfiedBy(it)) {
				return mapper.transform(it);
			}
			return ImmutableList.of();
		});
	}

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return delegate.filter(Specs.intersect(this.spec, spec));
	}
}
