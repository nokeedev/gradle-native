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
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Callable;

import static com.google.common.util.concurrent.Callables.returning;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.utils.TaskDependencyUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TaskDependencyUtils_OfTest {
	private static final Task T0 = Mockito.mock(Task.class);
	private static final Task T1 = Mockito.mock(Task.class);
	private static final Provider<Task> P0 = providerFactory().provider(returning(T0));
	private static final Provider<Task> P1 = providerFactory().provider(returning(T1));
	private static final Task ANY = Mockito.mock(Task.class);

	@Test
	void canCreateTaskDependencyFromTaskProvider() {
		assertThat(of(P0).getDependencies(ANY), contains(T0));
	}

	@Test
	void canCreateTaskDependencyFromTask() {
		assertThat(of(T0).getDependencies(ANY), contains(T0));
	}

	@Test
	void doesNotResolveProviderUponCreation() {
		assertDoesNotThrow(() -> of(providerFactory().provider(throwingCallable())));
	}

	private static Callable<Task> throwingCallable() {
		return () -> { throw new UnsupportedOperationException("do not realize"); };
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(P0), of(P0))
			.addEqualityGroup(of(P1))
			.addEqualityGroup(of(T0), of(T0))
			.addEqualityGroup(of(T1))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(of(P0),
			hasToString("TaskDependencyUtils.of(provider(?))"));
	}
}
