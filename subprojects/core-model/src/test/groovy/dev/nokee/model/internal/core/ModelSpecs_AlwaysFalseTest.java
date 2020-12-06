package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelSpecs.alwaysFalse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelSpecs_AlwaysFalseTest {
	@Test
	void noOpinionOnPath() {
		assertThat(alwaysFalse().getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(alwaysFalse().getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(alwaysFalse().getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertFalse(alwaysFalse().isSatisfiedBy(node()));
	}

	@Test
	void checkToString() {
		assertThat(alwaysFalse(), hasToString("ModelSpecs.alwaysFalse()"));
	}
}
