package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelNodes.and;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_AndTest {
	@Test
	void checkToString() {
		assertThat(and(alwaysTrue(), alwaysFalse()), hasToString("ModelNodes.and(Predicates.alwaysTrue(), Predicates.alwaysFalse())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(and(alwaysTrue(), alwaysFalse()), and(alwaysTrue(), alwaysFalse()))
			.addEqualityGroup(and(alwaysTrue(), alwaysTrue()))
			.addEqualityGroup(and(alwaysFalse(), alwaysTrue()))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByParent() {
		val predicate = and(it -> it.getPath().get().endsWith("a"), it -> it.getPath().get().startsWith("foo"));
		assertTrue(predicate.test(node("foo.a")));
		assertFalse(predicate.test(node("bar.a")));
		assertFalse(predicate.test(node("foo.b")));
	}
}
