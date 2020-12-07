package dev.nokee.model.internal.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodePredicateTest {
	@Test
	void canCreateSpecMatchingAllDirectDescendants() {
		val spec = NodePredicate.allDirectDescendants().scope(path("foo"));
		assertThat(spec.getPath(), emptyOptional());
		assertThat(spec.getParent(), optionalWithValue(equalTo(path("foo"))));
		assertThat(spec.getAncestor(), emptyOptional());
		assertTrue(spec.isSatisfiedBy(node()));
	}
}
