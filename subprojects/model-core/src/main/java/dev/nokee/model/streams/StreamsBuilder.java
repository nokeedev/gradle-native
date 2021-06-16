package dev.nokee.model.streams;

public interface StreamsBuilder {
	<T> ModelStream<T> stream(String topic);
}
