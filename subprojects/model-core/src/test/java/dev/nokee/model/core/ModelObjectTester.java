package dev.nokee.model.core;

import dev.nokee.model.BaseProjection;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.utils.ConsumerTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface ModelObjectTester<T> extends KnownDomainObjectTester<T> {
	ModelObject<T> subject();

	@Test
	default void canConfigureCurrentObject() {
		val action = ConsumerTestUtils.mockConsumer();
		subject().configure(action);
		assertThat(action, calledOnceWith(singleArgumentOf(subject())));
	}

	@Test
	default void returnThisModelObjectWhenConfiguring() {
		assertThat(subject().configure(ConsumerTestUtils.aConsumer()), is(subject()));
	}

	@Test
	default void canCreateNewProperty() {
		val property = assertDoesNotThrow(() -> subject().newProperty("foo", BaseProjection.class));
		assertThat(property, notNullValue());
	}
}
