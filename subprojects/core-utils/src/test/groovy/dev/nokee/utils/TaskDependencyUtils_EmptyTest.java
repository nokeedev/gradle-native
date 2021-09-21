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

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.util.Set;

import static dev.nokee.utils.TaskDependencyUtils.composite;
import static dev.nokee.utils.TaskDependencyUtils.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TaskDependencyUtils_EmptyTest {
	private static final Task T0 = Mockito.mock(Task.class);
	private static final Task T1 = Mockito.mock(Task.class);
	private static final TaskDependency D0 = task -> ImmutableSet.of(T0, T1);
	private static final Task ANY = Mockito.mock(Task.class);

	@Test
	void hasNoTasks() {
		assertThat(empty().getDependencies(ANY), emptyIterable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(empty(), empty())
			.addEqualityGroup(D0)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(empty(), hasToString("TaskDependencyUtils.empty()"));
	}
}
