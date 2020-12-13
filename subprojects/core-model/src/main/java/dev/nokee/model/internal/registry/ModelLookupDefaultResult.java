package dev.nokee.model.internal.registry;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.ModelNode;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ModelLookupDefaultResult implements ModelLookup.Result {
	private final List<ModelNode> result;

	public ModelLookupDefaultResult(List<ModelNode> result) {
		this.result = ImmutableList.copyOf(result);
	}

	@Override
	public List<ModelNode> get() {
		return result;
	}

	public <R> List<R> map(Function<? super ModelNode, R> mapper) {
		return get().stream().map(mapper).collect(ImmutableList.toImmutableList());
	}

	public void forEach(Consumer<? super ModelNode> action) {
		get().forEach(action);
	}
}
