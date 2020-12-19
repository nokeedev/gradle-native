package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.withParent;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_WithParentTest {
	@Test
	void checkToString() {
		assertThat(withParent(path("po.ta.to")), hasToString("ModelNodes.withParent(po.ta.to)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(withParent(path("foo")), withParent(path("foo")))
			.addEqualityGroup(withParent(path("bar")))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByParent() {
		val predicate = withParent(path("foo"));
		assertTrue(predicate.test(node("foo.a")));
		assertFalse(predicate.test(node("bar.a")));
		assertFalse(predicate.test(node("foo.a.b")));
	}
}
