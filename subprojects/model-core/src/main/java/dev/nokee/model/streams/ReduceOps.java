package dev.nokee.model.streams;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.provider.Provider;

import java.util.Objects;
import java.util.function.BinaryOperator;

public class ReduceOps {

	static abstract class ReduceOp<T, R, S extends AccumulatingSink<T, R, S>> implements TerminalOp<T, Provider<R>> {
		protected abstract S makeSink();

		@Override
		public <P_IN> Provider<R> evaluate(Pipeline<T> h, Topic<P_IN> supplier) {
			return ProviderUtils.supplied(() -> h.wrapAndCopyInto(makeSink(), supplier).get());
		}
	}

	/**
	 * A type of {@code TerminalSink} that implements an associative reducing operation on elements of type {@code T} and producing a result of type
	 * {@code R}.
	 *
	 * @param <T> the type of input element to the combining operation
	 * @param <R> the result type
	 * @param <K> the type of the {@code AccumulatingSink}.
	 */
	private interface AccumulatingSink<T, R, K extends AccumulatingSink<T, R, K>> extends TerminalSink<T, R> {
		void combine(K other);
	}


	/**
	 * Constructs a {@code TerminalOp} that implements a functional reduce on reference values producing an optional reference result.
	 *
	 * @param <T> The type of the input elements, and the type of the result
	 * @param operator The reducing function
	 * @return A {@code TerminalOp} implementing the reduction
	 */
	public static <T> TerminalOp<T, Provider<T>> makeRef(BinaryOperator<T> operator) {
		Objects.requireNonNull(operator);
		class ReducingSink implements AccumulatingSink<T, T, ReducingSink> {
			private boolean empty;
			private T state;

			public void begin(long size) {
				empty = true;
				state = null;
			}

			@Override
			public void accept(T t) {
				if (empty) {
					empty = false;
					state = t;
				} else {
					state = operator.apply(state, t);
				}
			}

			@Override
			public T get() {
				return empty ? null : state;
			}

			@Override
			public void combine(ReducingSink other) {
				if (!other.empty)
					accept(other.state);
			}
		}
		return new ReduceOp<T, T, ReducingSink>() {
			@Override
			public ReducingSink makeSink() {
				return new ReducingSink();
			}
		};
	}
}
