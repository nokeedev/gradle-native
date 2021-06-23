package dev.nokee.utils;

import org.junit.jupiter.api.Test;

import static dev.nokee.utils.ActionTestUtils.doSomething;

public interface ActionTester<T> {
	ActionUtils.Action<T> createSubject();

	@Test
	default void canChainWithVanillaGradleAction() {
		createSubject().andThen(doSomething()); // Static compilation test...
	}
}
