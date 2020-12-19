package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelSpecs.of;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ModelSpecs.class)
class ModelSpecs_OfPredicateTest {
	private final ModelSpec subject = of(withPath(path("po.ta.to")));

	@Test
	void noOpinionOnPath() {
		assertThat(subject.getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(subject.getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(subject.getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertTrue(subject.isSatisfiedBy(node("po.ta.to")));
		assertFalse(subject.isSatisfiedBy(node("po.ta")));
		assertFalse(subject.isSatisfiedBy(node("po.ta.to.yeah")));
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("ModelSpecs.of(ModelNodes.withPath(po.ta.to))"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(alwaysTrue()), of(alwaysTrue()))
			.addEqualityGroup(of(withPath(path("po.ta.to"))))
			.testEquals();
	}
}
