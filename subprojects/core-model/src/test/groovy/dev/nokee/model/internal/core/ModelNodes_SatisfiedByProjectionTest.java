package dev.nokee.model.internal.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.isSatisfiedByProjection;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_SatisfiedByProjectionTest {
	@Test
	void checkToString() {
		assertThat(isSatisfiedByProjection(MyType.class, satisfyAll()),
			hasToString("ModelNodes.isSatisfiedByProjection(interface dev.nokee.model.internal.core.ModelNodes_SatisfiedByProjectionTest$MyType, SpecUtils.satisfyAll())"));
	}

	@Test
	void returnsTheValueFromTheSpec() {
		val node = node(projectionOf(MyType.class));
		assertTrue(isSatisfiedByProjection(MyType.class, satisfyAll()).test(node));
		assertFalse(isSatisfiedByProjection(MyType.class, satisfyNone()).test(node));
	}

	@Test
	void returnsFalseWhenNodeHasNoProjection() {
		assertFalse(isSatisfiedByProjection(MyType.class, satisfyAll()).test(node(projectionOf(WrongType.class))));
	}

	interface MyType {}
	interface WrongType {}
}
