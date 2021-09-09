package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.TestProjection;
import groovy.lang.MissingPropertyException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.utils.ActionTestUtils.mockAction;
import static dev.nokee.utils.ClosureTestUtils.mockClosure;
import static dev.nokee.utils.ConsumerTestUtils.mockBiConsumer;
import static dev.nokee.utils.ConsumerTestUtils.mockConsumer;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Deprecated
public interface ModelNodeCreateChildNodeTester {
	ModelNode createSubject();

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#string")
	default void canCreateChildNode(NodeMethods.Identity method) {
		val node = method.invoke(createSubject(), "test");
		assertAll(
			() -> assertThat(node, isA(ModelNode.class)),
			() -> assertThat(node.getName(), equalTo("test"))
		);
	}

	@ParameterizedTest(name = "throws missing property exception when accessing non-existing node as property [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#property")
	default void throwsMissingPropertyExceptionWhenAccessingNonExistingNodeIdentity(NodeMethods.Identity method) {
		assertThrows(MissingPropertyException.class, () -> method.invoke(createSubject(), "test"));
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringAction")
	default void canCreateChildNode_StringAction(NodeMethods.IdentityAction method) {
		val action = mockAction(ModelNode.class);
		val node = method.invoke(createSubject(), "test", action);
		assertAll(
			() -> assertThat(node, isA(ModelNode.class)),
			() -> assertThat(node.getName(), equalTo("test")),
			() -> assertThat(action, calledOnceWith(singleArgumentOf(node)))
		);
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringClosure")
	default void canCreateChildNode_StringClosure(NodeMethods.IdentityClosure method) {
		val closure = mockClosure(ModelNode.class);
		val node = method.invoke(createSubject(), "test", closure);
		assertAll(
			() -> assertThat(node, isA(ModelNode.class)),
			() -> assertThat(node.getName(), equalTo("test")),
			() -> assertThat(closure, calledOnceWith(singleArgumentOf(node))),
			() -> assertThat(closure, calledOnceWith(delegateOf(node))),
			() -> assertThat(closure, calledOnceWith(delegateFirstStrategy()))
		);
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjection")
	default void canCreateChildNode_StringProjection(NodeMethods.IdentityProjection method) {
		assertDoesNotThrow(() -> method.invoke(createSubject(), "test", TestProjection.class));
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canCreateChildNode_StringProjectionAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockAction();
		assertDoesNotThrow(() -> method.invoke(createSubject(), "test", TestProjection.class, action));
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canCreateChildNode_StringProjectionBiAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockBiConsumer();
		assertDoesNotThrow(() -> method.invoke(createSubject(), "test", TestProjection.class, action));
		assertThat(action, calledOnceWith(firstArgumentOf(isA(ModelNode.class))));
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canCreateChildNode_StringProjectionClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(KnownDomainObject.class);
		assertDoesNotThrow(() -> method.invoke(createSubject(), "test", TestProjection.class, closure));
		assertAll(
			() -> assertThat(closure, calledOnceWith(delegateOf(allOf(isA(ModelNode.class), named("test"))))),
			() -> assertThat(closure, calledOnceWith(delegateFirstStrategy()))
		);
	}

	@ParameterizedTest(name = "can create child node [{argumentsWithNames} - BiClosure]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canCreateChildNode_StringProjectionBiClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(ModelNode.class, KnownDomainObject.class);
		assertDoesNotThrow(() -> method.invoke(createSubject(), "test", TestProjection.class, closure));
		assertAll(
			() -> assertThat(closure, calledOnceWith(firstArgumentOf(isA(ModelNode.class)))),
			() -> assertThat(closure, calledOnceWith(delegateOf(allOf(isA(ModelNode.class), named("test"))))),
			() -> assertThat(closure, calledOnceWith(delegateFirstStrategy()))
		);
	}
}
