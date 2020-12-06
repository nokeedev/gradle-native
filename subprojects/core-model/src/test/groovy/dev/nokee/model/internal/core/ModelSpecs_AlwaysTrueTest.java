package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelSpecs.alwaysTrue;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelSpecs_AlwaysTrueTest {
	@Test
	void noOpinionOnPath() {
		assertThat(alwaysTrue().getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(alwaysTrue().getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(alwaysTrue().getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertTrue(alwaysTrue().isSatisfiedBy(node()));
	}

	@Test
	void checkToString() {
		assertThat(alwaysTrue(), hasToString("ModelSpecs.alwaysTrue()"));
	}
}
