/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelActions.once;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestActions.doSomethingElse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

class ModelActions_OnceTest {
	@Test
	void executeDelegateActionOnlyOnce() {
		val node = node();
		val delegate = mock(ModelAction.class);
		val action = once(delegate);
		action.execute(node);
		action.execute(node);

		verify(delegate, times(1)).execute(node);
	}

	@Test
	void executeDelegateActionOncePerNode() {
		val node1 = node("foo");
		val node2 = node("bar");
		val delegate = mock(ModelAction.class);
		val action = once(delegate);
		action.execute(node1);
		action.execute(node2);

		verify(delegate, times(1)).execute(node1);
		verify(delegate, times(1)).execute(node2);
	}

	@Test
	void checkToString() {
		assertThat(once(doSomething()), hasToString("ModelActions.once(ModelTestActions.doSomething())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(once(doSomething()), once(doSomething()), alreadyExecutedAction(doSomething()))
			.addEqualityGroup(once(doSomethingElse()))
			.testEquals();
	}

	private static ModelAction alreadyExecutedAction(ModelAction delegate) {
		val executedAction = once(delegate);
		executedAction.execute(node());
		return executedAction;
	}
}
