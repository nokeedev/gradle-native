package dev.nokee.model.streams;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Represent a stream topic providing elements as well as accepting new elements.
 *
 * @param <E_IN>  type of elements of the topic
 */
public abstract class Topic<E_IN> implements Sink<E_IN>, Source<E_IN> {
	private final List<Sink<E_IN>> sinks = new ArrayList<>();
	private final ModelStream<E_IN> stream = new DefaultModelStream<>(this);

	final void addSink(Sink<E_IN> sink) {
		sinks.add(sink);
	}

	@Override
	public void accept(E_IN t) {
		sinks.forEach(it -> it.accept(t));
	}

	public ModelStream<E_IN> stream() {
		return stream;
	}

	/**
	 * Create a topic of the supplied element stream.
	 *
	 * @param supplier  a supplier of elements, must not be null
	 * @param <T>  type of elements
	 * @return a topic for the supplied elements, never null
	 */
	public static <T> Topic<T> of(Supplier<? extends Stream<T>> supplier) {
		requireNonNull(supplier);
		return new Topic<T>() {
			@Override
			public Stream<T> get() {
				return supplier.get();
			}
		};
	}
}
