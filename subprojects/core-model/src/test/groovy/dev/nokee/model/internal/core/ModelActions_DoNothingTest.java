package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelActions.doNothing;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ModelActions_DoNothingTest {
	@Test
	void doesNothing() {
		assertDoesNotThrow(() -> doNothing().execute(node()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(doNothing(), doNothing())
			.addEqualityGroup((ModelAction) node -> { /* something */})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(doNothing(), hasToString("ModelActions.doNothing()"));
	}
}
