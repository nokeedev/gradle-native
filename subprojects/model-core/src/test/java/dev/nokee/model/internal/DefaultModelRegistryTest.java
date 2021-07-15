package dev.nokee.model.internal;

import dev.nokee.model.registry.ModelRegistry;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultModelRegistryTest {
	private final ModelRegistry subject = new DefaultModelRegistry();

	@Test
	void hasRootNode() {
		assertThat(subject.getRoot(), notNullValue());
	}

	@Test
	void rootNodeHasRootIdentity() {
		assertThat(subject.getRoot().getIdentity(), equalTo(DomainObjectIdentities.root()));
	}

	@Test
	void rootNodeDoesNotHaveParent() {
		assertThat(subject.getRoot().getParent(), emptyOptional());
	}

	@Test /** @see dev.nokee.model.internal.DefaultModelRegistryAllProjectionsStreamTest */
	void newRegistryHasNoProjectionInStream() {
		assertThat(subject.allProjections().collect(toList()), providerOf(emptyIterable()));
	}
}
