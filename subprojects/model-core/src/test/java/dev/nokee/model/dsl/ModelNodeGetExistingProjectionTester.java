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
import static dev.nokee.utils.ConsumerTestUtils.mockConsumer;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Deprecated
public interface ModelNodeGetExistingProjectionTester {
	ModelNode createSubject();

	static KnownDomainObject<TestProjection> withExistingProjection(ModelNode node) {
		return node.projection(TestProjection.class);
	}

	@Test
	default void canGetProjection() {
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject);
		assertThat("returns existing projection", subject.projection(TestProjection.class), is(existingProjection));
	}

	@Test
	default void canGetProjection_TypeAction() {
		val action = mockAction(TestProjection.class);
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject);
		val knownObject = assertDoesNotThrow(() -> subject.projection(TestProjection.class, action));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat("defer projection creation", action, neverCalled())
		);
	}

	@Test
	default void canGetProjection_TypeClosure() {
		val closure = mockClosure(TestProjection.class);
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject);
		val knownObject = assertDoesNotThrow(() -> subject.projection(TestProjection.class, closure));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat("defer projection creation", closure, neverCalled())
		);
	}

	@ParameterizedTest(name = "can get projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjection")
	default void canGetProjection_StringProjection(NodeMethods.IdentityProjection method) {
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject.node("test"));
		assertThat("returns existing projection",
			method.invoke(subject, "test", TestProjection.class), is(existingProjection));
	}

	@ParameterizedTest(name = "can create projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canGetProjection_StringProjectionAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockAction();
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject.node("test"));
		val knownObject = assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, action));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat("defer projection creation", action, neverCalled())
		);
	}

	@ParameterizedTest(name = "registers configure action on existing projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void registersConfigureActionOnExistingProjection_StringProjectionAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockAction();
		val subject = createSubject();
		withExistingProjection(subject.node("test"));
		val knownObject = method.invoke(subject, "test", TestProjection.class, action);
		knownObject.map(it -> it).get(); // realize
		assertThat(action, calledOnceWith(singleArgumentOf(isA(TestProjection.class))));
	}

	@ParameterizedTest(name = "can create projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canGetProjection_StringProjectionBiAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockBiConsumer();
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject.node("test"));
		val knownObject = assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, action));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat(action, calledOnceWith(secondArgumentOf(existingProjection)))
		);
	}

	@ParameterizedTest(name = "can get projection [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canGetProjection_StringProjectionClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(KnownDomainObject.class);
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject.node("test"));
		val knownObject = assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, closure));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat(closure, calledOnceWith(singleArgumentOf(existingProjection)))
		);
	}

	@ParameterizedTest(name = "can get projection [{argumentsWithNames} - BiClosure]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canGetProjection_StringProjectionBiClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(ModelNode.class, KnownDomainObject.class);
		val subject = createSubject();
		val existingProjection = withExistingProjection(subject.node("test"));
		val knownObject = assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, closure));
		assertAll(
			() -> assertThat("returns existing projection", knownObject, is(existingProjection)),
			() -> assertThat(closure, calledOnceWith(secondArgumentOf(existingProjection)))
		);
	}
}
