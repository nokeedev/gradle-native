package dev.nokee.model.streams;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

/** @see ModelStream#empty() */
enum EmptyModelStream implements ModelStream<Object> {
	INSTANCE;

	public <T> ModelStream<T> withNarrowedType() {
		return (ModelStream<T>) this;
	}

	@Override
	public BranchedModelStream<Object> split() {
		return EmptyBranchedModelStream.INSTANCE.withNarrowedType();
	}

	@Override
	public ModelStream<Object> filter(Predicate<? super Object> predicate) {
		return this;
	}

	@Override
	public <R> ModelStream<R> flatMap(Function<? super Object, ? extends Iterable<? extends R>> mapper) {
		return withNarrowedType();
	}

	@Override
	public <R> ModelStream<R> map(Function<? super Object, ? extends R> mapper) {
		return withNarrowedType();
	}

	@Override
	public ModelStream<Object> peek(Consumer<? super Object> action) {
		return this;
	}

	@Override
	public void forEach(Consumer<? super Object> action) {
		// nothing to do
	}

	@Override
	public <R, A> Provider<R> collect(Collector<? super Object, A, R> collector) {
		return ProviderUtils.notDefined();
	}
}
