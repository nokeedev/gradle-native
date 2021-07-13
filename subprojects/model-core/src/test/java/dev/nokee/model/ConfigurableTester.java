package dev.nokee.model;

import com.google.common.reflect.TypeToken;
import dev.nokee.model.util.Configurable;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static dev.nokee.utils.FunctionalInterfaceMatchers.calledOnceWith;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface ConfigurableTester<T> {
	Configurable<T> createSubject();

	@Test
	default void canConfigureUsingAction() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.configure(action);
		assertThat(action, calledOnceWith(singleArgumentOf(subject)));
	}

	@Test
	default void returnsConfigurableTypeWhenConfiguring() {
		assertAll(
			() -> assertThat(createSubject().configure(ActionTestUtils.doSomething()), isA(typeOf(this))),
			() -> assertThat(createSubject().configure(ClosureTestUtils.doSomething(typeOf(this))), isA(typeOf(this))),
			() -> assertThat(createSubject().configure(TestNokeeExtensionRule.class), isA(typeOf(this)))
		);
	}

	@Test
	default void canConfigureUsingClass() {
		val subject = createSubject();
		subject.configure(TestNokeeExtensionRule.class);
		assertThat(TestNokeeExtensionRule.capturedExecution, calledOnceWith(singleArgumentOf(subject)));
	}

	@Test
	default void canConfigureUsingClosure() {
		val closure = ClosureTestUtils.mockClosure(Object.class);
		val subject = createSubject();
		subject.configure(closure);
		assertThat(closure, calledOnceWith(singleArgumentOf(subject)));
	}

	static <T> Class<T> typeOf(ConfigurableTester<T> self) {
		return (Class<T>) new TypeToken<T>(self.getClass()) {}.getRawType();
	}

	class TestNokeeExtensionRule implements Action<Object> {
		static ActionTestUtils.MockAction<Object> capturedExecution;

		@Inject
		public TestNokeeExtensionRule() {
			capturedExecution = ActionTestUtils.mockAction();
		}

		@Override
		public void execute(Object t) {
			capturedExecution.execute(t);
		}
	}
}
