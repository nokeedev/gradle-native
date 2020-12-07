package dev.nokee.model.internal.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodePredicateTest {
	@Test
	void canCreateSpecMatchingAllDirectDescendants() {
		val spec = allDirectDescendants().scope(path("foo"));
		assertThat(spec.getPath(), emptyOptional());
		assertThat(spec.getParent(), optionalWithValue(equalTo(path("foo"))));
		assertThat(spec.getAncestor(), emptyOptional());
		assertTrue(spec.isSatisfiedBy(node()));
	}

	@Test
	void canCreateSpecMatchingAllDirectDescendantsAndPredicate() {
		val spec = allDirectDescendants(alwaysFalse()).scope(path("bar"));
		assertThat(spec.getPath(), emptyOptional());
		assertThat(spec.getParent(), optionalWithValue(equalTo(path("bar"))));
		assertThat(spec.getAncestor(), emptyOptional());
		assertFalse(spec.isSatisfiedBy(node()));
	}
}
