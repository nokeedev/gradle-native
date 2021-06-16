package dev.nokee.model.streams;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public interface BranchedFactoryTester<T> {
	Branched<T> createSubject();

	@Test
	default void canCreateBranched() {
		assertThat(createSubject(), notNullValue(Branched.class));
	}
}
