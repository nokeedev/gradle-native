package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyAll;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelSpecs_SatisfyAllTest {
	@Test
	void noOpinionOnPath() {
		assertThat(satisfyAll().getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(satisfyAll().getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(satisfyAll().getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertTrue(satisfyAll().isSatisfiedBy(node()));
	}

	@Test
	void checkToString() {
		assertThat(satisfyAll(), hasToString("ModelSpecs.satisfyAll()"));
	}
}
