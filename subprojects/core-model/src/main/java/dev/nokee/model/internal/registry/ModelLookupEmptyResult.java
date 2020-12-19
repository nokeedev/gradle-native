package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelNode;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

enum ModelLookupEmptyResult implements ModelLookup.Result {
	INSTANCE;

	@Override
	public List<ModelNode> get() {
		return ImmutableList.of();
	}

	@Override
	public <R> List<R> map(Function<? super ModelNode, R> mapper) {
		return ImmutableList.of();
	}

	@Override
	public void forEach(Consumer<? super ModelNode> action) {
		// do nothing.
	}
}
