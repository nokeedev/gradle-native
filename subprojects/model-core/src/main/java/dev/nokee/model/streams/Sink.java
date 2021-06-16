package dev.nokee.model.streams;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represent the stages of a stream pipeline processing values.
 *
 * @param <T>  type of elements to sink
 */
interface Sink<T> extends Consumer<T> {

	static abstract class Chained<T, E_OUT> implements Sink<T> {
		protected final Sink<? super E_OUT> downstream;

		public Chained(Sink<? super E_OUT> downstream) {
			this.downstream = Objects.requireNonNull(downstream);
		}
	}
}
