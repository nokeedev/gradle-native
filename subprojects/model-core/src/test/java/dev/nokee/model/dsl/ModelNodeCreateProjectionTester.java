package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.TestProjection;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.utils.ActionTestUtils.mockAction;
import static dev.nokee.utils.ClosureTestUtils.mockClosure;
import static dev.nokee.utils.ConsumerTestUtils.mockBiConsumer;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface ModelNodeCreateProjectionTester {
	ModelNode createSubject();

	@Test
	default void canCreateProjection() {
		val knownObject = createSubject().projection(TestProjection.class);
		assertThat(knownObject.getType(), equalTo(TestProjection.class));
	}

	@Test
	default void canCreateProjection_TypeAction() {
		val action = mockAction(TestProjection.class);
		val knownObject = createSubject().projection(TestProjection.class, action);
		assertAll(
			() -> assertThat(knownObject.getType(), equalTo(TestProjection.class)),
			() -> assertThat("defer projection creation", action, neverCalled())
		);
	}

	@Test
	default void canCreateProjection_TypeClosure() {
		val closure = mockClosure(TestProjection.class);
		val knownObject = createSubject().projection(TestProjection.class, closure);
		assertAll(
			() -> assertThat("returns known domain object", knownObject, isA(KnownDomainObject.class)),
			() -> assertThat(knownObject.getType(), equalTo(TestProjection.class)),
			() -> assertThat("defer projection creation", closure, neverCalled())
		);
	}

	@ParameterizedTest(name = "can create projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjection")
	default void canCreateProjection_StringProjection(NodeMethods.IdentityProjection method) {
		val knownObject = method.invoke(createSubject(), "test", TestProjection.class);
		assertAll(
			() -> assertThat("returns known domain object", knownObject, isA(KnownDomainObject.class)),
			() -> assertThat(knownObject.getType(), is(TestProjection.class))
		);
	}

	@Test
	default void canCreateProjection_StringProjectionBiAction() {
		val action = mockBiConsumer();
		val knownObject = createSubject().node("test", TestProjection.class, action);
		assertAll(
			() -> assertThat("returns known domain object", knownObject, isA(KnownDomainObject.class)),
			() -> assertThat(knownObject.getType(), is(TestProjection.class)),
			() -> assertThat(action, calledOnceWith(secondArgumentOf(knownObject)))
		);
	}

	@ParameterizedTest(name = "can create projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canCreateProjection_StringProjectionClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(KnownDomainObject.class);
		val knownObject = method.invoke(createSubject(), "test", TestProjection.class, closure);
		assertAll(
			() -> assertThat("returns known domain object", knownObject, isA(KnownDomainObject.class)),
			() -> assertThat(knownObject.getType(), is(TestProjection.class)),
			() -> assertThat(closure, calledOnceWith(singleArgumentOf(knownObject)))
		);
	}

	@ParameterizedTest(name = "can create projection [{argumentsWithNames} - BiClosure]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canCreateProjection_StringProjectionBiClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(ModelNode.class, KnownDomainObject.class);
		val knownObject = method.invoke(createSubject(), "test", TestProjection.class, closure);
		assertAll(
			() -> assertThat("returns known domain object", knownObject, isA(KnownDomainObject.class)),
			() -> assertThat(knownObject.getType(), is(TestProjection.class)),
			() -> assertThat(closure, calledOnceWith(secondArgumentOf(knownObject)))
		);
	}
}
