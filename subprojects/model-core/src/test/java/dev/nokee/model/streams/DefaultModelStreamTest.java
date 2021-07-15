package dev.nokee.model.streams;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

class DefaultModelStreamTest implements ModelStreamTester<Object> {
	private final List<Object> list = new ArrayList<>();
	private final Topic<Object> topic = Topic.of(list::stream);

	@Override
	public ModelStream<Object> createSubject() {
		return new DefaultModelStream<>(topic);
	}

	@Override
	public Object createElement() {
		val result = new Object();
		list.add(result);
		topic.accept(result);
		return result;
	}
}
