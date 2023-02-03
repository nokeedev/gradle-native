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
package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.invocations.InvocationMatchers;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.FunctionalInterfaceMatchers;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.narrowed;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface ConfigurableSourceSetTester extends SourceSetTester {
	ConfigurableSourceSet subject();

	@Test
	default void hasFilter() {
		assertThat(subject().getFilter(), isA(PatternFilterable.class));
	}

	@Test
	default void canConfigureFilterUsingAction() {
		TestDouble<Action<PatternFilterable>> action = newMock(Action.class);
		subject().filter(action.instance());
		assertThat(action.to(method(Action<PatternFilterable>::execute)), calledOnceWith(subject().getFilter()));
	}

	@Test
	default void canConfigureFilterUsingClosure() {
		ClosureTestUtils.MockClosure<Void, PatternFilterable> closure = ClosureTestUtils.mockClosure(PatternFilterable.class);
		subject().filter(closure);
		assertThat(closure, FunctionalInterfaceMatchers.calledOnceWith(singleArgumentOf(subject().getFilter())));
		assertThat(closure, FunctionalInterfaceMatchers.calledOnceWith(allOf(delegateOf(subject().getFilter()), delegateFirstStrategy())));
	}

	@Test
	default void canAddPathsToSourceSet() {
		assertDoesNotThrow(() -> subject().from("some/path"));
	}

	@Test
	default void returnSourceSetWhenAddingPaths() {
		assertThat(subject().from("some/path"), is(subject()));
	}

	@Test
	default void canAddConventionToSourceSet() {
		assertDoesNotThrow(() -> subject().convention("some/path"));
	}

	@Test
	default void returnSourceSetWhenAddingConvention() {
		assertThat(subject().convention("some/path"), is(subject()));
	}
}
