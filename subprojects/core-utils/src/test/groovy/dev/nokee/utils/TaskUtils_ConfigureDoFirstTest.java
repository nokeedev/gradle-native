package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.ActionTestUtils.doSomethingElse;
import static dev.nokee.utils.TaskUtils.configureDoFirst;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskUtils_ConfigureDoFirstTest {
	@Test
	void throwsExceptionIfActionIsLambda() {
		assertThrows(RuntimeException.class, () -> configureDoFirst(task -> {}));
	}

	@Test
	void doesNotThrowExceptionIfActionIsAnonymousClass() {
		assertDoesNotThrow(() -> configureDoFirst(new Action<Task>() {
			@Override
			public void execute(Task task) {
				// do something
			}
		}));
	}

	@Test
	void forwardsActionToTaskDoFirstMethod() {
		val task = Mockito.mock(Task.class);
		configureDoFirst(doSomething()).execute(task);
		Mockito.verify(task).doFirst(doSomething());
	}

	@Test
	void forwardsNamedActionToTaskDoFirstNamedMethod() {
		val task = Mockito.mock(Task.class);
		val action = new NamedTaskAction("test");
		configureDoFirst(action).execute(task);
		Mockito.verify(task).doFirst("test", action);
	}

	@EqualsAndHashCode
	private static final class NamedTaskAction implements Action<Task>, Named {
		private final String name;

		private NamedTaskAction(String name) {
			this.name = name;
		}

		@Override
		public void execute(Task task) {
			// do something
		}

		@Override
		public String getName() {
			return name;
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureDoFirst(doSomething()), configureDoFirst(doSomething()))
			.addEqualityGroup(configureDoFirst(doSomethingElse()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDoFirst(doSomething()), hasToString("TaskUtils.configureDoFirst(doSomething())"));
		assertThat(configureDoFirst(doSomethingElse()), hasToString("TaskUtils.configureDoFirst(doSomethingElse())"));
	}
}
