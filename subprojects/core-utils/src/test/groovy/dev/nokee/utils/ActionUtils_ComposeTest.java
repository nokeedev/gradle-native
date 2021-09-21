/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import dev.nokee.internal.testing.ExecuteWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.ActionTestUtils.doSomethingElse;
import static dev.nokee.utils.ActionUtils.compose;
import static dev.nokee.utils.ActionUtils.doNothing;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
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
