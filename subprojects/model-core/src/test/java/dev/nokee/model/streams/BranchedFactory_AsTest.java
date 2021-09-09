package dev.nokee.model.streams;

import java.util.stream.Stream;

class BranchedFactory_AsTest implements BranchedFactoryTester<Object>, BranchedTester<Object> {
	@Override
	public Branched<Object> createSubject() {
		return Branched.as("test");
	}

	@Override
	public BranchedModelStream<Object> createStream() {
		return new DefaultBranchedModelStream<>(new DefaultModelStream<>(Topic.of(Stream::empty)));
	}
}
