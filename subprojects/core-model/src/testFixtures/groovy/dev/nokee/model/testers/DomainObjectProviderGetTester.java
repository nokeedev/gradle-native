package dev.nokee.model.testers;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public abstract class DomainObjectProviderGetTester<T> extends AbstractDomainObjectProviderTester<T> {
	@Test
	void resolvesProviderWhenQueried() {
		val provider = createSubject();
		provider.get();
		expectResolved(provider);
	}

	@Test
	void returnsProviderValueWhenQueried() {
		assertThat(createSubject().get(), isA(getSubjectType()));
	}
}
