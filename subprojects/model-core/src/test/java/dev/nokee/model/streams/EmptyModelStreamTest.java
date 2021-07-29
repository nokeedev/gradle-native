package dev.nokee.model.streams;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;

class EmptyModelStreamTest {
	@Test
	void returnsProviderOfEmptyList() {
		assertThat(ModelStream.empty().collect(toList()), providerOf(emptyIterable()));
	}
}
