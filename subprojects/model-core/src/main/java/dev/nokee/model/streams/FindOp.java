package dev.nokee.model.streams;

import dev.nokee.utils.ProviderUtils;
import org.gradle.api.provider.Provider;

import java.util.function.Supplier;

final class FindOp<T, O> implements TerminalOp<T, Provider<O>> {
	private final Supplier<TerminalSink<T, O>> sinkSupplier;

	FindOp(Supplier<TerminalSink<T, O>> sinkSupplier) {
		this.sinkSupplier = sinkSupplier;
	}

	@Override
	public <P_IN> Provider<O> evaluate(Pipeline<T> h, Topic<P_IN> supplier) {
		return ProviderUtils.supplied(() -> h.wrapAndCopyInto(sinkSupplier.get(), supplier).get());
	}
}
