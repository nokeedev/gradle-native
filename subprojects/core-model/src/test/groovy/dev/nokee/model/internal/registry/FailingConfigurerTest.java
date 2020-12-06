package dev.nokee.model.internal.registry;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelActions.doNothing;
import static dev.nokee.model.internal.core.ModelSpecs.alwaysTrue;
import static dev.nokee.model.internal.registry.ModelConfigurer.failingConfigurer;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailingConfigurerTest {
	@Test
	void throwsException() {
		assertThrows(UnsupportedOperationException.class, () -> failingConfigurer().configureMatching(alwaysTrue(), doNothing()));
	}
}
