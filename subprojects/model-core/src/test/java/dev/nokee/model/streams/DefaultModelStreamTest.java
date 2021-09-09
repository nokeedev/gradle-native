package dev.nokee.model.streams;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

class DefaultModelStreamTest implements ModelStreamIntegrationTester<Object> {
	private final List<Object> list = new ArrayList<>();
	private final Topic<Object> topic = Topic.of(list::stream);
	private final ModelStream<Object> subject = new DefaultModelStream<>(topic);

	@Override
	public ModelStream<Object> subject() {
		return subject;
	}

	@Override
	public Object createElement() {
		val result = new Object();
		list.add(result);
		topic.accept(result);
		return result;
	}
}
