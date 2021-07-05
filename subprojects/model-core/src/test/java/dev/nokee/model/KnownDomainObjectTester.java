package dev.nokee.model;

import com.google.common.testing.NullPointerTester;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.KnownDomainObjectTestUtils.getType;
import static dev.nokee.model.KnownDomainObjectTestUtils.realize;
import static dev.nokee.utils.ActionTestUtils.mockAction;
import static dev.nokee.utils.ClosureTestUtils.mockClosure;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static dev.nokee.utils.ProviderUtils.fixed;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface KnownDomainObjectTester<T> {
	KnownDomainObject<TestProjection> createSubject();

	@Test
	default void hasIdentifier() {
		assertThat(createSubject().getIdentifier(), notNullValue(DomainObjectIdentifier.class));
	}

	@Test
	default void hasType() {
		assertThat(createSubject().getType(), is(getType(this)));
	}

	@Test
	default void canMapKnownObject() {
		assertThat(createSubject().map(it -> "foo"), providerOf("foo"));
	}

	@Test
	default void canFlatMapKnownObject() {
		assertThat(createSubject().flatMap(it -> fixed("bar")), providerOf("bar"));
	}

	@Test
	default void returnsThisKnownObjectWhenConfiguring() {
		val subject = createSubject();
		assertAll(
			() -> assertThat(subject.configure(mockAction()), is(subject)),
			() -> assertThat(subject.configure(mockClosure(getType(this))), is(subject))
		);
	}

	@Test
	default void canConfigureKnownObjectUsingAction() {
		val action = ActionTestUtils.mockAction();
		realize(createSubject().configure(action));
		assertThat(action, calledOnceWith(singleArgumentOf(isA(getType(this)))));
	}

	@Test
	default void canConfigureKnownObjectUsingClosure() {
		val closure = mockClosure(getType(this));
		realize(createSubject().configure(closure));
		assertThat(closure, calledOnceWith(singleArgumentOf(isA(getType(this)))));
		assertThat(closure, calledOnceWith(allOf(delegateFirstStrategy(), delegateOf(isA(getType(this))))));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}
}
