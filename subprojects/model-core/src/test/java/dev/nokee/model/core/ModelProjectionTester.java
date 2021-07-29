package dev.nokee.model.core;

import com.google.common.testing.NullPointerTester;
import dev.nokee.model.UnknownProjection;
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.GradleProviderMatchers.absentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
	default void canViewProjectionAsProvider() {
		assertThat(createSubject().canBeViewedAs(Provider.class), is(true));
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
	default void canGetProjectionAsProvider() {
		assertThat(createSubject().get(new TypeOf<Provider<TestProjection>>() {}.getConcreteClass()), isA(Provider.class));
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

	@Test
	default void hasOwnerNode() {
		assertThat(createSubject().getOwner(), isA(ModelNode.class));
	}

	@Test
	default void hasType() {
		assertThat(createSubject().getType(), is(TestProjection.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	//region Model projection finalizer
	@Test
	default void canRegisterFinalizeActionOfKnownType() {
		assertDoesNotThrow(() -> createSubject().whenFinalized(TestProjection.class, doSomething()));
	}

	@Test
	default void doesNotExecuteFinalizeActionWhenProjectionIsNotFinalized() {
		val action = ActionTestUtils.mockAction();
		createSubject().whenFinalized(TestProjection.class, action);
		assertThat(action, neverCalled());
	}

	@Test
	default void executesFinalizeActionWhenProjectionIsRealizedAfterFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(TestProjection.class, action);
		subject.finalizeProjection();
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canFinalizeProjectionMultipleTime() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(TestProjection.class, action);
		subject.finalizeProjection();
		subject.finalizeProjection();
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void throwsExceptionWhenRegisterFinalizeActionOfUnknownType() {
		val ex = assertThrows(RuntimeException.class, () -> createSubject().whenFinalized(UnknownProjection.class, doSomething()));
		assertThat(ex.getMessage(), equalTo("Projection cannot be viewed as 'UnknownProjection'."));
	}

	@Test
	default void canMarkProjectionToRealizeWhenFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(TestProjection.class, action);
		subject.realizeOnFinalize();
		subject.finalizeProjection();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canMarkProjectionToRealizeWhenFinalizedAfterProjectionIsAlreadyFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(TestProjection.class, action);
		subject.finalizeProjection();
		subject.realizeOnFinalize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void flushesNewFinalizeActionWhenProjectionIsAlreadyFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.finalizeProjection();
		subject.whenFinalized(TestProjection.class, action);
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canChainRealizeOnFinalized() {
		val subject = createSubject();
		assertThat(subject.realizeOnFinalize(), is(subject));
	}
	//endregion

	interface BaseProjection {}
	interface TestProjection extends BaseProjection, Named {}
	final class DefaultTestProjection implements TestProjection {
		private final String name;

		public DefaultTestProjection(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
