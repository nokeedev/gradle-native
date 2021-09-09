package dev.nokee.model.streams;

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.provider.Provider;

import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

abstract class AbstractModelStream<E_IN, E_OUT> extends Pipeline<E_OUT> implements ModelStream<E_OUT> {
	private final Topic<?> topic;
	private final int depth;
	private final AbstractModelStream<?, E_IN> previousStage;

	AbstractModelStream(Topic<?> topic) {
		this.topic = topic;
		this.depth = 0;
		this.previousStage = null;
	}

	private AbstractModelStream(AbstractModelStream<?, E_IN> previousStage) {
		this.topic = previousStage.topic;
		this.depth = previousStage.depth + 1;
		this.previousStage = previousStage;
	}

	abstract Sink<E_IN> opWrapSink(Sink<E_OUT> sink);

	final <R> R evaluate(TerminalOp<E_OUT, R> terminalOp) {
		return terminalOp.evaluate(this, topic);
	}

	@Override
	final <P_IN, S extends Sink<E_OUT>> S wrapAndCopyInto(S sink, Source<P_IN> source) {
		copyInto(wrapSink(requireNonNull(sink)), source);
		return sink;
	}

	@Override
	final <P_IN> void copyInto(Sink<P_IN> wrappedSink, Source<P_IN> source) {
		int skipCount = 0;
		for (;;) {
			val items = source.get().skip(skipCount).collect(Collectors.toList());
			if (items.size() == 0) {
				return;
			}
			wrappedSink.begin(items.size());
			items.forEach(wrappedSink);
			wrappedSink.end();
			skipCount += items.size();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	final <P_IN> Sink<P_IN> wrapSink(Sink<E_OUT> sink) {
		requireNonNull(sink);

		for (@SuppressWarnings("rawtypes") AbstractModelStream p = AbstractModelStream.this; p.depth > 0; p = p.previousStage) {
			sink = p.opWrapSink(sink);
		}
		return (Sink<P_IN>) sink;
	}



	//		assert getOutputShape() == terminalOp.inputShape();
//		if (linkedOrConsumed)
//			throw new IllegalStateException(MSG_STREAM_LINKED);
//		linkedOrConsumed = true;
//
//		return isParallel()
//			? terminalOp.evaluateParallel(this, sourceSpliterator(terminalOp.getOpFlags()))
//			: terminalOp.evaluateSequential(this, sourceSpliterator(terminalOp.getOpFlags()));

	// todo test element created while mapping filtering for each

	@Override
	public BranchedModelStream<E_OUT> split() {
		return new DefaultBranchedModelStream<>(this);
	}

	@Override
	public ModelStream<E_OUT> filter(Predicate<? super E_OUT> predicate) {
		requireNonNull(predicate);
		return new AbstractModelStream<E_OUT, E_OUT>(this) {
			@Override
			Sink<E_OUT> opWrapSink(Sink<E_OUT> sink) {
				return new Sink.Chained<E_OUT, E_OUT>(sink) {
					@Override
					public void accept(E_OUT e) {
						if (predicate.test(e)) {
							downstream.accept(e);
						}
					}
				};
			}
		};
	}

	@Override
	public <R> ModelStream<R> flatMap(Function<? super E_OUT, ? extends Iterable<? extends R>> mapper) {
		requireNonNull(mapper);
		return new AbstractModelStream<E_OUT, R>(this) {
			@Override
			Sink<E_OUT> opWrapSink(Sink<R> sink) {
				return new Sink.Chained<E_OUT, R>(sink) {
					@Override
					public void accept(E_OUT e) {
						mapper.apply(e).forEach(downstream::accept);
					}
				};
			}
		};
	}

	@Override
	public <R> ModelStream<R> map(Function<? super E_OUT, ? extends R> mapper) {
		requireNonNull(mapper);
		return new AbstractModelStream<E_OUT, R>(this) {
			@Override
			Sink<E_OUT> opWrapSink(Sink<R> sink) {
				return new Sink.Chained<E_OUT, R>(sink) {
					@Override
					public void accept(E_OUT e) {
						downstream.accept(mapper.apply(e));
					}
				};
			}
		};
	}

	@Override
	public ModelStream<E_OUT> sorted(Comparator<? super E_OUT> comparator) {
		requireNonNull(comparator);
		return new AbstractModelStream<E_OUT, E_OUT>(this) {
			@Override
			Sink<E_OUT> opWrapSink(Sink<E_OUT> sink) {
				return new Sink.Sorting<>(sink, comparator);
			}
		};
	}

	@Override
	public Provider<E_OUT> findFirst() {
		return evaluate(new FindOp<>(Sink.Find::new));
	}

	// TODO: Test return value
	@Override
	public ModelStream<E_OUT> peek(Consumer<? super E_OUT> action) {
		evaluate(new ForEachOp<>(action));
		return this;
	}

	@Override
	public void forEach(Consumer<? super E_OUT> action) {
		evaluate(new ForEachOp<>(action));
	}

	@Override
	public <R, A> Provider<R> collect(Collector<? super E_OUT, A, R> collector) {
		return evaluate(new CollectorOp<>(collector));
	}

	@Override
	public Provider<E_OUT> reduce(BinaryOperator<E_OUT> accumulator) {
		return evaluate(ReduceOps.makeRef(accumulator));
	}

	@Override
	public Provider<E_OUT> min(Comparator<? super E_OUT> comparator) {
		return reduce(BinaryOperator.minBy(comparator));
	}

	@Override
	public Provider<E_OUT> max(Comparator<? super E_OUT> comparator) {
		return reduce(BinaryOperator.maxBy(comparator));
	}
}
