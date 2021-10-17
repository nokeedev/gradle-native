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

import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyAll;
import static dev.nokee.model.internal.core.ModelSpecs.satisfyNone;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.ModelTestActions.doSomethingElse;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

class ModelActions_MatchingTest {
	@Test
	void executesActionIfPredicateIsTrue() {
		val action = mock(ModelAction.class);
		val node = node();
		matching(satisfyAll(), action).execute(node);
		verify(action, times(1)).execute(node);
	}

	@Test
	void doesNotExecuteActionIfPredicateIsFalse() {
		val action = mock(ModelAction.class);
		val node = node();
		matching(satisfyNone(), action).execute(node);
		verify(action, never()).execute(node);
	}

	@Test
	void checkToString() {
		assertThat(matching(satisfyAll(), doSomething()),
			hasToString("ModelActions.matching(ModelSpecs.satisfyAll(), ModelTestActions.doSomething())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(matching(satisfyAll(), doSomething()), matching(satisfyAll(), doSomething()))
			.addEqualityGroup(matching(satisfyNone(), doSomething()))
			.addEqualityGroup(matching(satisfyAll(), doSomethingElse()))
			.testEquals();
	}
}
