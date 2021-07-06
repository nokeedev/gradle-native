package dev.nokee.model.core;

import dev.nokee.model.UnknownProjection;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface ModelProjectionTester {
	ModelProjection createSubject();

	@Test
	default void canViewAsProjectionType() {
		assertThat(createSubject().canBeViewedAs(TestProjection.class), is(true));
	}

	@Test
	default void canViewAsProjectionBaseType() {
		assertThat(createSubject().canBeViewedAs(BaseProjection.class), is(true));
	}

	@Test
	default void cannotViewAsProjectionImplementationType() {
		assertThat(createSubject().canBeViewedAs(DefaultTestProjection.class), is(false));
	}

	@Test
	default void returnsPresentProviderForViewableType() {
		assertThat(createSubject().as(TestProjection.class), providerOf(isA(TestProjection.class)));
	}

	@Test
	default void returnsAbsentProviderForNonViewableType() {
		assertThat(createSubject().as(UnknownProjection.class), absentProvider());
	}

	@Test
	default void throwsExceptionWhenResolvingProjectionForNonViewableType() {
		val ex = assertThrows(RuntimeException.class, () -> createSubject().get(UnknownProjection.class));
		assertThat(ex.getMessage(), is(""));
	}

	@Test
	default void canResolveProjectionForViewableType() {
		assertThat(createSubject().get(TestProjection.class), isA(TestProjection.class));
	}

	@Test
	default void canExecuteProjectionWhenRealized() {
		val action = Mockito.mock(Action.class);
		val subject = createSubject();
		subject.whenRealized(TestProjection.class, action);
		subject.get(TestProjection.class); //realize
		Mockito.verify(action).execute(ArgumentMatchers.isA(TestProjection.class));
	}

	@Test
	default void throwsExceptionWhenConfiguringNonViewableType() {
		val subject = createSubject();
		assertThrows(RuntimeException.class, () -> subject.whenRealized(UnknownProjection.class, doSomething()));
	}

	interface BaseProjection {}
	interface TestProjection extends BaseProjection {}
	final class DefaultTestProjection implements TestProjection {}
}
