package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyNone;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelSpecs_SatisfyNoneTest {
	@Test
	void noOpinionOnPath() {
		assertThat(satisfyNone().getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(satisfyNone().getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(satisfyNone().getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertFalse(satisfyNone().isSatisfiedBy(node()));
	}

	@Test
	void checkToString() {
		assertThat(satisfyNone(), hasToString("ModelSpecs.satisfyNone()"));
	}
}
