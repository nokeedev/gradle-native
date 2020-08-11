package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestableView<T> implements View<T> {
	private final List<T> backingCollection;

	public TestableView(T... elements) {
		this(Arrays.asList(elements));
	}

	public TestableView(List<T> backingCollection) {
		this.backingCollection = backingCollection;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		backingCollection.forEach(action::execute);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		backingCollection.forEach(element -> {
			if (type.isAssignableFrom(element.getClass())) {
				action.execute(type.cast(element));
			}
		});
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		backingCollection.forEach(element -> {
			if (spec.isSatisfiedBy(element)) {
				action.execute(element);
			}
		});
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Provider<Set<? extends T>> getElements() {
		return Providers.of(ImmutableSet.copyOf(backingCollection));
	}

	@Override
	public Set<? extends T> get() {
		return ImmutableSet.copyOf(backingCollection);
	}

	@Override
	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(elements -> elements.stream().map(mapper::transform).collect(Collectors.toList()));
	}

	@Override
	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return getElements().map(elements -> elements.stream().map(mapper::transform).flatMap(it -> ImmutableList.copyOf(it).stream()).collect(Collectors.toList()));
	}

	@Override
	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return getElements().map(elements -> elements.stream().filter(spec::isSatisfiedBy).collect(Collectors.toList()));
	}
}
