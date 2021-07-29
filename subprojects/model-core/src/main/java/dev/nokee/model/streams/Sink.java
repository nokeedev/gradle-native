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

	default boolean cancellationRequested() {
		return false;
	}

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

		@Override
		public boolean cancellationRequested() {
			return downstream.cancellationRequested();
		}
	}

	static final class Sorting<T> extends Chained<T, T> {
		private final Comparator<? super T> comparator;
		private ArrayList<T> list;
		private boolean cancellationWasRequested;

		public Sorting(Sink<? super T> downstream, Comparator<? super T> comparator) {
			super(downstream);
			this.comparator = comparator;
		}

		@Override
		public void begin(long size) {
			assert list == null; // Quick fix to ensure no reuse are done... for now.
			list = (size >= 0) ? new ArrayList<>((int) size) : new ArrayList<>();
		}

		@Override
		public void end() {
			list.sort(comparator);
			downstream.begin(list.size());
			if (!cancellationWasRequested) {
				list.forEach(downstream::accept);
			} else {
				for (int i = 0; i < list.size() && !downstream.cancellationRequested(); ++i) {
					downstream.accept(list.get(i));
				}
			}
			downstream.end();
			list = null;
		}

		/**
		 * Records is cancellation is requested so short-circuiting behaviour can be preserved when the sorted elements are pushed downstream.
		 *
		 * @return false, as this sink never short-circuits.
		 */
		@Override
		public boolean cancellationRequested() {
			cancellationWasRequested = true;
			return false;
		}

		@Override
		public void accept(T t) {
			list.add(t);
		}
	}

	static final class Find<T> implements TerminalSink<T, T> {
		boolean hasValue;
		T value;

		Find() {} // Avoid creation of special accessor

		@Override
		public void accept(T value) {
			if (!hasValue) {
				hasValue = true;
				this.value = value;
			}
		}

		@Override
		public boolean cancellationRequested() {
			return hasValue;
		}

		@Override
		public T get() {
			return value;
		}
	}
}
