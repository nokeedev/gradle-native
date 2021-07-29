package dev.nokee.model.streams;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represent the stages of a stream pipeline processing values.
 *
 * @param <T>  type of elements to sink
 */
interface Sink<T> extends Consumer<T> {

	default void begin(long size) {}

	default void end() {}

	static abstract class Chained<T, E_OUT> implements Sink<T> {
		protected final Sink<? super E_OUT> downstream;

		public Chained(Sink<? super E_OUT> downstream) {
			this.downstream = Objects.requireNonNull(downstream);
		}

		@Override
		public void begin(long size) {
			downstream.begin(size);
		}

		@Override
		public void end() {
			downstream.end();
		}
	}

	static final class Sorting<T> extends Chained<T, T> {
		private final Comparator<? super T> comparator;
		private ArrayList<T> list;

		public Sorting(Sink<? super T> downstream, Comparator<? super T> comparator) {
			super(downstream);
			this.comparator = comparator;
		}

		@Override
		public void begin(long size) {
			assert list == null; // Quick fix to ensure no reuse are done... for now.
			list = (size >= 0) ? new ArrayList<T>((int) size) : new ArrayList<T>();
		}

		@Override
		public void end() {
			list.sort(comparator);
			downstream.begin(list.size());
			list.forEach(downstream::accept);
			downstream.end();
			list = null;
		}

		@Override
		public void accept(T t) {
			list.add(t);
		}
	}
}
