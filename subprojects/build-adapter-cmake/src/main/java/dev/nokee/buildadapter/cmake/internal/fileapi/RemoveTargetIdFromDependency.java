package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.val;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class RemoveTargetIdFromDependency implements UnaryOperator<CodeModelTarget> {
	private final String targetIdToRemove;

	public RemoveTargetIdFromDependency(String targetIdToRemove) {
		this.targetIdToRemove = targetIdToRemove;
	}

	@Override
	public CodeModelTarget apply(CodeModelTarget target) {
		assert target != null;
		val filteredDependencies = target.getDependencies().stream().filter(it -> !it.getId().equals(targetIdToRemove)).collect(Collectors.toList());
		return target.withDependencies(filteredDependencies);
	}
}
