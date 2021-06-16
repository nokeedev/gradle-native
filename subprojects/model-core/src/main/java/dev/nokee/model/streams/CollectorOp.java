package dev.nokee.model.streams;

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.provider.Provider;

import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

final class CollectorOp<A, E_IN, R> implements TerminalOp<E_IN, Provider<R>> {
	private final Collector<? super E_IN, A, R> collector;

	public CollectorOp(Collector<? super E_IN, A, R> collector) {
		this.collector = requireNonNull(collector);
	}

	@Override
	public <P_IN> Provider<R> evaluate(Pipeline<E_IN> h, Topic<P_IN> supplier) {
		return ProviderUtils.supplied(() -> {
			val container = collector.supplier().get();
			val accu = collector.accumulator();

			h.wrapAndCopyInto(it -> {
				accu.accept(container, it);
			}, supplier/*.get().spliterator()*/);

			return collector.finisher().apply(container);
		});
	}
}
