package dev.nokee.model.internal.core;

import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

	@ParameterizedTest
	@EnumSource(WithType.class)
	void canCreateSpecMatchingAllDirectDescendantsOfSpecificType(WithType withType) {
		val spec = withType.invoke(NodePredicate.allDirectDescendants(), MyType.class).scope(path("foo"));
		assertThat(spec.getPath(), emptyOptional());
		assertThat(spec.getParent(), optionalWithValue(equalTo(path("foo"))));
		assertThat(spec.getAncestor(), emptyOptional());
		assertTrue(spec.isSatisfiedBy(node(projectionOf(MyType.class))));
		assertFalse(spec.isSatisfiedBy(node()));
	}
	interface WithTypeMethod {
		NodePredicate invoke(NodePredicate target, Class<?> type);
	}
	enum WithType implements WithTypeMethod {
		WITH_TYPE_USING_MODEL_TYPE() {
			@Override
			public NodePredicate invoke(NodePredicate target, Class<?> type) {
				return target.withType(type);
			}
		},
		WITH_TYPE_USING_CLASS() {
			@Override
			public NodePredicate invoke(NodePredicate target, Class<?> type) {
				return target.withType(ModelType.of(type));
			}
		};
	}

	@Test
	void canCreateSpecMatchingAllDirectDescendantsOfSpecificState() {
		val spec = NodePredicate.allDirectDescendants().stateAtLeast(ModelNode.State.Registered).scope(path("foo"));
		assertThat(spec.getPath(), emptyOptional());
		assertThat(spec.getParent(), optionalWithValue(equalTo(path("foo"))));
		assertThat(spec.getAncestor(), emptyOptional());
		assertFalse(spec.isSatisfiedBy(node()));
		assertTrue(spec.isSatisfiedBy(node().register()));
		assertTrue(spec.isSatisfiedBy(node().realize()));
	}

	interface MyType {}
}
