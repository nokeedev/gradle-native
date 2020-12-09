package dev.nokee.model.internal.core;

import com.google.common.base.Predicates;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A predicate for matching model node more efficiently.
 */
// TODO: Maybe we could achieve a more minimalist API by using a raw-ish Predicate where the additional methods for efficiency are hidden from the users.
//   We will revisit this at a later time.
public interface ModelPredicate {
	default Optional<ModelPath> getPath() {
		return Optional.empty();
	}

	default Optional<ModelPath> getParent() {
		return Optional.empty();
	}

	default Optional<ModelPath> getAncestor() {
		return Optional.empty();
	}

	default Predicate<? super ModelNode> getMatcher() {
		return Predicates.alwaysTrue();
	}
}
