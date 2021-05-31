package dev.nokee.model;

import dev.nokee.model.dsl.ModelNode;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.ActionTestUtils.mockAction;
import static dev.nokee.utils.ClosureTestUtils.mockClosure;
import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface NokeeExtensionTester {
	NokeeExtension createSubject();

	@Test
	default void canGetModel() {
		assertThat(createSubject().getModel(), isA(ModelNode.class));
	}

	@Test
	default void canConfigureModelUsingAction() {
		val action = mockAction(ModelNode.class);
		createSubject().model(action);
		assertThat(action, calledOnceWith(singleArgumentOf(isA(ModelNode.class))));
	}

	@Test
	default void canConfigureModelUsingClosure() {
		val closure = mockClosure(ModelNode.class);
		createSubject().model(closure);
		assertThat(closure, calledOnceWith(singleArgumentOf(isA(ModelNode.class))));
	}

	@Test
	default void isExtensionAware() {
		assertAll(
			() -> assertThat(ExtensionAware.class.isAssignableFrom(NokeeExtension.class), is(true)),
			() -> assertThat(createSubject(), isA(ExtensionAware.class))
		);
	}
}
