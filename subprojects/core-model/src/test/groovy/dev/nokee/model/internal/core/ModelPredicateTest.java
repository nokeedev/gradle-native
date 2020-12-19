package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static org.hamcrest.MatcherAssert.assertThat;

// We are testing the interface defaults.
public class ModelPredicateTest {
	private final ModelPredicate subject = new ModelPredicate() {};

	@Test
	void defaultsToEmptyPath() {
		assertThat(subject.getPath(), emptyOptional());
	}

	@Test
	void defaultsToEmptyParent() {
		assertThat(subject.getParent(), emptyOptional());
	}

	@Test
	void defaultsToEmptyAncestor() {
		assertThat(subject.getAncestor(), emptyOptional());
	}
}
