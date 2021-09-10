package dev.nokee.model;

import com.google.common.reflect.TypeToken;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;

public interface SelfMutatingNamedDomainObjectProviderTester<T> {
	NamedDomainObjectProvider<T> createSubject(String name);

	// IMPORTANT: Use this method instead of {@code self.configure(action)} as the *raw* API is not *shielded* against self-mutation.
	@SuppressWarnings("rawtypes")
	NamedDomainObjectProvider configure(NamedDomainObjectProvider self, Action action);

	@SuppressWarnings("UnstableApiUsage")
	default Class<? super T> type() {
		return new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Test
	default void canConfigureSelfWhileConfiguringDomainObject() {
		val subject = createSubject("gjti");
		assertDoesNotThrow(() -> configure(subject, it -> configure(subject, ActionTestUtils.doSomething())).get());
	}

	@Test
	default void throwsExceptionWhenMutatingAnotherDomainObjectOfTheSameContainerWhileConfiguring_Self_DomainObject() {
		val subject = createSubject("ksje");
		val otherSubject = createSubject("dkes");
		assertThrows(RuntimeException.class, () -> configure(subject, it -> configure(otherSubject, ActionTestUtils.doSomething())).get());
	}

	@Test
	@SuppressWarnings("unchecked")
	default void executesSelfMutationActionAfterAlreadyRegisteredActions() {
		val a0 = Mockito.mock(Action.class);
		val a1 = Mockito.mock(Action.class);
		val a2 = Mockito.mock(Action.class);
		val inOrder = Mockito.inOrder(a0, a2, a1);
		val subject = createSubject("kdfw");
		configure(subject, a0);
		configure(subject, it -> configure(subject, a1));
		configure(subject, a2);

		// when:
		subject.get();

		// then:
		inOrder.verify(a0).execute(isA(type()));
		inOrder.verify(a2).execute(isA(type()));
		inOrder.verify(a1).execute(isA(type()));
	}
}
