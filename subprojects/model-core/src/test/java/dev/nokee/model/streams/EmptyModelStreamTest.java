package dev.nokee.model.streams;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.streams.ModelStreamTester.alwaysLastElement;
import static dev.nokee.model.streams.ModelStreamTester.noParticularOrder;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;

class EmptyModelStreamTest implements ModelStreamTester<Object> {
	@Override
	public ModelStream<Object> subject() {
		return ModelStream.empty();
	}

	@Test
	void returnsProviderOfEmptyListOnCollect() {
		assertThat(ModelStream.empty().collect(toList()), providerOf(emptyIterable()));
	}

	@Test
	void returnsAbsentProviderOnReduction() {
		assertThat(subject().reduce(alwaysLastElement()), absentProvider());
	}

	@Test
	void returnsAbsentProviderOnMinimumReduction() {
		assertThat(subject().min(noParticularOrder()), absentProvider());
	}

	@Test
	void returnsAbsentProviderOnMaximumReduction() {
		assertThat(subject().max(noParticularOrder()), absentProvider());
	}

	@Test
	void returnsAbsentProviderOnFirstFind() {
		assertThat(subject().findFirst(), absentProvider());
	}
}
