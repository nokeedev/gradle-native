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

class TaskDependencyUtils_CompositeTest {
	private static final Task T0 = Mockito.mock(Task.class);
	private static final Task T1 = Mockito.mock(Task.class);
	private static final Task T2 = Mockito.mock(Task.class);
	private static final Task T3 = Mockito.mock(Task.class);
	private static final TaskDependency D0 = new TaskDependency() {
		@Override
		public Set<? extends Task> getDependencies(@Nullable Task task) {
			return ImmutableSet.of(T0, T1);
		}

		@Override
		public String toString() {
			return "task dependencies [0]";
		}
	};
	private static final TaskDependency D1 = new TaskDependency() {
		@Override
		public Set<? extends Task> getDependencies(@Nullable Task task) {
			return ImmutableSet.of(T2);
		}

		@Override
		public String toString() {
			return "task dependencies [1]";
		}
	};
	private static final TaskDependency D2 = task -> ImmutableSet.of(T3);
	private static final Task ANY = Mockito.mock(Task.class);

	@Test
	void canComposeMultipleTaskDependencies() {
		assertThat(composite(D0, D1).getDependencies(ANY), contains(T0, T1, T2));
	}

	@Test
	void returnsSingleTaskDependencyWhenOnlyOneInstance() {
		assertThat(composite(D0), is(D0));
	}

	@Test
	void returnsSingleTaskDependencyWhenSameInstanceMultipleTime() {
		assertThat(composite(D0, D0), is(D0));
	}

	@Test
	void returnsEmptyTaskDependencyWhenNoInstance() {
		assertThat(composite(), is(empty()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(composite(D0, D1), composite(D0, D1), composite(D1, D0))
			.addEqualityGroup(composite(D0, D2))
			.addEqualityGroup(composite(D1, D2))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(composite(D0, D1),
			hasToString("TaskDependencyUtils.composite(task dependencies [0], task dependencies [1])"));
	}
}
