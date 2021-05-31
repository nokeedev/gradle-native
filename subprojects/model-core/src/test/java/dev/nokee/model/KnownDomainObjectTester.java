package dev.nokee.model;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.ProviderUtils.fixed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public interface KnownDomainObjectTester {
	KnownDomainObject<TestProjection> createSubject();

	@Test
	default void hasIdentifier() {
		assertThat(createSubject().getIdentifier(), notNullValue(DomainObjectIdentifier.class));
	}

	@Test
	default void hasType() {
		assertThat(createSubject().getType(), is(TestProjection.class));
	}

	@Test
	default void canMapKnownObject() {
		assertThat(createSubject().map(it -> "foo"), providerOf("foo"));
	}

	@Test
	default void canFlatMapKnownObject() {
		assertThat(createSubject().flatMap(it -> fixed("bar")), providerOf("bar"));
	}
}
