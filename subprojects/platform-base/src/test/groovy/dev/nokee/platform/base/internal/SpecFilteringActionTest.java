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
package dev.nokee.platform.base.internal;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.ActionTestUtils.doSomethingElse;
import static dev.nokee.utils.SpecTestUtils.aSpec;
import static dev.nokee.utils.SpecTestUtils.anotherSpec;
import static org.mockito.Mockito.*;

class SpecFilteringActionTest {
	@SuppressWarnings("unchecked") private final Spec<Object> spec = (Spec<Object>) Mockito.mock(Spec.class);
	@SuppressWarnings("unchecked") private final Action<Object> action = (Action<Object>) Mockito.mock(Action.class);
	private final SpecFilteringAction<Object> subject = new SpecFilteringAction<>(spec, action);
	private final Object trueValue = new Object();
	private final Object falseValue = new Object();

	@BeforeEach
	void setUp() {
		when(spec.isSatisfiedBy(trueValue)).thenReturn(true);
		when(spec.isSatisfiedBy(falseValue)).thenReturn(false);
	}

	@Test
	void executesActionWhenSpecIsSatisfied() {
		subject.execute(trueValue);
		verify(action).execute(trueValue);
	}

	@Test
	void doesNotExecutesActionWhenSpecIsNotSatisfied() {
		subject.execute(falseValue);
		verifyNoInteractions(action);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicConstructors(SpecFilteringAction.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(new SpecFilteringAction<>(aSpec(), doSomething()), new SpecFilteringAction<>(aSpec(), doSomething()))
			.addEqualityGroup(new SpecFilteringAction<>(anotherSpec(), doSomething()))
			.addEqualityGroup(new SpecFilteringAction<>(anotherSpec(), doSomethingElse()))
			.testEquals();
	}
}
