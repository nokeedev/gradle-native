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

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Callable;

import static com.google.common.util.concurrent.Callables.returning;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.TaskDependencyUtils.ofIterable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TaskDependencyUtils_OfIterableTest {
	private static final Task T0 = Mockito.mock(Task.class);
	private static final Task T1 = Mockito.mock(Task.class);
	private static final Provider<Iterable<Task>> P0 = providerFactory().provider(returning(ImmutableList.of(T0, T1)));
	private static final Provider<Iterable<Task>> P1 = providerFactory().provider(returning(ImmutableList.of(T1)));
	private static final Task ANY = Mockito.mock(Task.class);

	@Test
	void canCreateTaskDependencyFromIterableOfTaskProvider() {
		assertThat(ofIterable(P0).getDependencies(ANY), contains(T0, T1));
	}

	@Test
	void doesNotResolveProviderUponCreation() {
		assertDoesNotThrow(() -> ofIterable(providerFactory().provider(throwingCallable())));
	}

	private static Callable<Iterable<Task>> throwingCallable() {
		return () -> { throw new UnsupportedOperationException("do not realize"); };
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofIterable(P0), ofIterable(P0))
			.addEqualityGroup(ofIterable(P1))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(ofIterable(P0),
			hasToString("TaskDependencyUtils.ofIterable(provider(?))"));
	}
}
