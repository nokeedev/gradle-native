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
