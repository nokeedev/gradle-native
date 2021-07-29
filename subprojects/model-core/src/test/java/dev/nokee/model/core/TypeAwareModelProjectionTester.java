package dev.nokee.model.core;

import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface TypeAwareModelProjectionTester extends ModelProjectionTester {
	@Override
	TypeAwareModelProjection<TestProjection> createSubject();

	//region Model projection finalizer
	@Test
	default void canRegisterFinalizeActionOfKnownType() {
		assertDoesNotThrow(() -> createSubject().whenFinalized(doSomething()));
	}

	@Test
	default void doesNotExecuteFinalizeActionWhenProjectionIsNotFinalized() {
		val action = ActionTestUtils.mockAction();
		createSubject().whenFinalized(action);
		assertThat(action, neverCalled());
	}

	@Test
	default void executesFinalizeActionWhenProjectionIsRealizedAfterFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(action);
		subject.finalizeProjection();
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canFinalizeProjectionMultipleTime() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(action);
		subject.finalizeProjection();
		subject.finalizeProjection();
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canMarkProjectionToRealizeWhenFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(action);
		subject.realizeOnFinalize();
		subject.finalizeProjection();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void canMarkProjectionToRealizeWhenFinalizedAfterProjectionIsAlreadyFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.whenFinalized(action);
		subject.finalizeProjection();
		subject.realizeOnFinalize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@Test
	default void flushesNewFinalizeActionWhenProjectionIsAlreadyFinalized() {
		val action = ActionTestUtils.mockAction();
		val subject = createSubject();
		subject.finalizeProjection();
		subject.whenFinalized(action);
		subject.realize();
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}
	//endregion
}
