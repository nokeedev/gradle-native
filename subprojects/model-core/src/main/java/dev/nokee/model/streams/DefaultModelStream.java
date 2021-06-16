package dev.nokee.model.streams;

final class DefaultModelStream<T> extends AbstractModelStream<T, T> {
	DefaultModelStream(Topic<?> topic) {
		super(topic);
	}

	@Override
	Sink<T> opWrapSink(Sink<T> sink) {
		return sink;
	}
}
