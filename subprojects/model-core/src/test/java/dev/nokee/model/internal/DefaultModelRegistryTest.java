package dev.nokee.model.internal;

import dev.nokee.model.registry.ModelRegistry;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

class DefaultModelRegistryTest {
	private final ModelRegistry subject = new DefaultModelRegistry();

	@Test
	void hasRootNode() {
		assertThat(subject.getRoot(), notNullValue());
	}

	@Test
	void rootNodeDoesNotHaveParent() {
		assertThat(subject.getRoot().getParent(), emptyOptional());
	}
}
