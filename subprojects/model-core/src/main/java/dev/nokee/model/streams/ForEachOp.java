package dev.nokee.model.streams;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

final class ForEachOp<T> implements TerminalOp<T, Void> {
	private final Consumer<? super T> action;

	public ForEachOp(Consumer<? super T> action) {
		this.action = requireNonNull(action);
	}

	@Override
	public <P_IN> Void evaluate(Pipeline<T> h, Topic<P_IN> topic) {
		Sink<P_IN> sink = h.wrapSink(action::accept);
		h.copyInto(sink, topic);
		topic.addSink(sink);
		return null;
	}
}
