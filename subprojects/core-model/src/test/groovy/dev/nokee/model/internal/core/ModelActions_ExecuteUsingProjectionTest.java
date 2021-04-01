package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.gradle.api.Action;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.gradleplugins.grava.util.ActionUtils.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

class ModelActions_ExecuteUsingProjectionTest {
	@Test
	void checkToString() {
		assertThat(executeUsingProjection(of(MyType.class), doNothing()), hasToString("ModelActions.executeUsingProjection(interface dev.nokee.model.internal.core.ModelActions_ExecuteUsingProjectionTest$MyType, ActionUtils.doNothing())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(executeUsingProjection(of(MyType.class), doNothing()), executeUsingProjection(of(MyType.class), doNothing()))
			.addEqualityGroup(executeUsingProjection(of(WrongType.class), doNothing()))
			.addEqualityGroup(executeUsingProjection(of(MyType.class), it -> { /* something */ }))
			.testEquals();
	}

	@Test
	void executeActionWhenProjectionIsAvailable() {
		Action<MyType> action = Cast.uncheckedCast(mock(Action.class));
		assertDoesNotThrow(() -> executeUsingProjection(of(MyType.class), action).execute(node(projectionOf(MyType.class))));
		verify(action, times(1)).execute(isA(MyType.class));
	}

	@Test
	void throwExceptionWhenProjectionIsUnavailable() {
		Action<WrongType> action = Cast.uncheckedCast(mock(Action.class));
		assertThrows(IllegalStateException.class, () -> executeUsingProjection(of(WrongType.class), action).execute(node(projectionOf(MyType.class))));
		verify(action, never()).execute(any());
	}

	interface MyType {}
	interface WrongType {}
}
