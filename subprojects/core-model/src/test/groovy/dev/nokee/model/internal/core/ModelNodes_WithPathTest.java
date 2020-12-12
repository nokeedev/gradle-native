package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_WithPathTest {
	@Test
	void checkToString() {
		assertThat(withPath(path("po.ta.to")), hasToString("ModelNodes.withPath(po.ta.to)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(withPath(path("foo")), withPath(path("foo")))
			.addEqualityGroup(withPath(path("bar")))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByPath() {
		val predicate = withPath(path("foo.a"));
		assertTrue(predicate.test(node("foo.a")));
		assertFalse(predicate.test(node("bar.a")));
		assertFalse(predicate.test(node("foo.a.b")));
		assertFalse(predicate.test(node("foo")));
	}
}
