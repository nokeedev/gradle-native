package dev.nokee.model.streams;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;

/** ModelStream#empty() */
enum EmptyBranchedModelStream implements BranchedModelStream<Object> {
	INSTANCE;

	public <T> BranchedModelStream<T> withNarrowedType() {
		return (BranchedModelStream<T>) this;
	}

	@Override
	public BranchedModelStream<Object> branch(Predicate<? super Object> predicate) {
		return this;
	}

	@Override
	public BranchedModelStream<Object> branch(Predicate<? super Object> predicate, Branched<Object> branched) {
		return this;
	}

	@Override
	public Map<String, ModelStream<Object>> defaultBranch() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, ModelStream<Object>> defaultBranch(Branched<Object> branched) {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, ModelStream<Object>> noDefaultBranch() {
		return Collections.emptyMap();
	}
}
