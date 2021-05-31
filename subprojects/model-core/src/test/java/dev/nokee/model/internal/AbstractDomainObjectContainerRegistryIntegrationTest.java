package dev.nokee.model.internal;

import dev.nokee.internal.testing.ExecuteWith;
import lombok.val;
import org.apache.commons.lang3.Functions;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static dev.nokee.internal.testing.ExecuteWith.calledOnceWith;
import static dev.nokee.internal.testing.ExecuteWith.executeWith;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract class AbstractDomainObjectContainerRegistryIntegrationTest<T> {
	protected abstract NamedDomainObjectContainerRegistry<T> createSubject();

	protected abstract Class<T> getType();

	@Test
	void usesNamingSchemeToDetermineNameFromIdentifier() {
		assertAll(
			assertObjectNamed("aaaBbb", s -> s.register("aaaBbb", getType())),
			assertObjectNamed("cccDdd", s -> s.register("cccDdd", getType(), doSomething())),
			assertObjectNamed("eeeFff", s -> s.registerIfAbsent("eeeFff", getType())),
			assertObjectNamed("gggHhh", s -> s.registerIfAbsent("gggHhh", getType(), doSomething()))
		);
	}
	private Executable assertObjectNamed(String expectedName, Functions.FailableFunction<NamedDomainObjectContainerRegistry<T>, NamedDomainObjectProvider<T>, Throwable> executable) {
		return () -> assertThat(executable.apply(createSubject()), named(expectedName));
	}

	@Test
	void canConfigureRegisteredObject() {
		assertAll(
			assertActionExecuted((s, action) -> s.register("a", getType(), action)),
			assertActionExecuted((s, action) -> s.registerIfAbsent("b", getType(), action))
		);
	}
	private Executable assertActionExecuted(Functions.FailableBiFunction<NamedDomainObjectContainerRegistry<T>, Action<? super T>, NamedDomainObjectProvider<T>, Throwable> executable) {
		return () -> {
			assertThat(executeWith(ExecuteWith.<T>action(action -> executable.apply(createSubject(), action).get())),
				calledOnceWith(isA(getType())));
		};
	}

	@Test
	void throwsExceptionForDuplicateRegistering() {
		assertAll(
			assertThrowsAlreadyExists(s -> s.register("a", getType())),
			assertThrowsAlreadyExists(s -> s.register("b", getType(), doSomething()))
		);
	}
	private Executable assertThrowsAlreadyExists(Functions.FailableFunction<NamedDomainObjectContainerRegistry<T>, NamedDomainObjectProvider<T>, Throwable> executable) {
		return () -> {
			val subject = createSubject();
			val ex = assertThrows(RuntimeException.class, () -> {
				executable.apply(subject);
				executable.apply(subject);
			});
			assertThat(ex.getMessage(), endsWith("with that name already exists."));
		};
	}

	@Test
	void canRegisterExistingObject() {
		assertAll(
			assertRegisterExistingObject(s -> s.registerIfAbsent("existing", getType())),
			assertRegisterExistingObject(s -> s.registerIfAbsent("existing", getType(), doSomething()))
		);
	}
	private Executable assertRegisterExistingObject(Functions.FailableFunction<NamedDomainObjectContainerRegistry<T>, NamedDomainObjectProvider<T>, Throwable> executable) {
		return () -> {
			val subject = createSubject();
			assertThat(executable.apply(subject), notNullValue());
			assertThat(executable.apply(subject), notNullValue());
		};
	}

//	interface Bean extends Named {
//		String getBeanProperty();
//		void setBeanProperty(String value);
//	}
//
//	@Data
//	static abstract class AbstractBean implements Bean {
//		public final String name;
//		String beanProperty;
//
//		public AbstractBean(String name) {
//			this.name = name;
//		}
//
//		@Override
//		public String toString() {
//			return name;
//		}
//	}
//
//	static final class DefaultBean extends AbstractBean {
//		@Inject
//		public DefaultBean(String name) {
//			super(name);
//		}
//	}
//
//	interface UnknownBean extends Bean {}
}
