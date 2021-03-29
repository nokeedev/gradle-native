package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.ExecuteWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.gradleplugins.grava.testing.util.ActionTestUtils.doSomething;
import static dev.gradleplugins.grava.testing.util.ActionTestUtils.doSomethingElse;
import static dev.gradleplugins.grava.util.ActionUtils.compose;
import static dev.gradleplugins.grava.util.ActionUtils.doNothing;
import static dev.gradleplugins.grava.testing.util.TransformerTestUtils.aTransformer;
import static dev.gradleplugins.grava.testing.util.TransformerTestUtils.anotherTransformer;
import static dev.gradleplugins.grava.util.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class ActionUtils_ComposeTest {
	@Test
	void checkToString() {
		assertThat(compose(doSomething(), aTransformer()),
			hasToString("ActionUtils.compose(doSomething(), aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(doSomething(), aTransformer()), compose(doSomething(), aTransformer()))
			.addEqualityGroup(compose(doSomethingElse(), aTransformer()))
			.addEqualityGroup(compose(doSomething(), anotherTransformer()))
			.testEquals();
	}

	@Test
	void returnsDoNothingActionWhenSpecifiedActionDoesNothing() {
		assertThat(compose(doNothing(), aTransformer()), equalTo(doNothing()));
	}

	@Test
	void returnsActionWhenSpecifiedTransformIsNoOp() {
		assertThat(compose(doSomething(), noOpTransformer()), equalTo(doSomething()));
	}

	@Nested
	class Execution {
		private final Object INPUT = new Object();
		private final Object TRANSFORMER_OUTPUT = new Object();
		private ExecuteWith.ExecutionResult<Object> transformerExecution;
		private ExecuteWith.ExecutionResult<Object> actionExecution;

		@BeforeEach
		void setUpComposeAction() {
			actionExecution = executeWith(action(g -> {
				transformerExecution = executeWith(transformer(f -> {
					compose(g, f).execute(INPUT);
				}).thenReturn(TRANSFORMER_OUTPUT));
			}));
		}

		@Test
		void canTransformInputBeforeCallingAction() {
			assertThat(actionExecution, calledOnceWith(TRANSFORMER_OUTPUT));
			assertThat(transformerExecution, calledOnceWith(INPUT));
		}
	}
}
