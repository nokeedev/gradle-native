package dev.nokee.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import com.google.common.util.concurrent.Callables;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.util.concurrent.Callables.returning;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
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
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(of(P0),
			hasToString("TaskDependencyUtils.of(provider(?))"));
	}
}
