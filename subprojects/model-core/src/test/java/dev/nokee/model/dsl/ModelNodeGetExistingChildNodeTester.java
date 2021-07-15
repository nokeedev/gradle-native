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
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public interface ModelNodeGetExistingChildNodeTester {
	ModelNode createSubject();

	static ModelNode withExistingChildNode(ModelNode node) {
		return node.node("test");
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#string")
	default void canGetChildNode(NodeMethods.Identity method) {
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		assertThat("returns existing child node", method.invoke(subject, "test"), is(childNode));
	}

	@ParameterizedTest(name = "can get child node as property [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#property")
	default void canGetChildNodeAsProperty(NodeMethods.Identity method) {
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		assertThat("returns existing child node", method.invoke(subject, "test"), is(childNode));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringAction")
	default void canGetChildNode_StringAction(NodeMethods.IdentityAction method) {
		val action = mockAction(ModelNode.class);
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		assertThat("returns existing child node", method.invoke(subject, "test", action), is(childNode));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringClosure")
	default void canGetChildNode_StringClosure(NodeMethods.IdentityClosure method) {
		val closure = mockClosure(ModelNode.class);
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		val node = method.invoke(subject, "test", closure);
		assertAll(
			() -> assertThat("returns existing child node", node, is(childNode)),
			() -> assertThat("calls back with existing child node",
				closure, calledOnceWith(allOf(singleArgumentOf(childNode), delegateOf(childNode), delegateFirstStrategy())))
		);
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjection")
	default void canGetChildNode_StringProjection(NodeMethods.IdentityProjection method) {
		val subject = createSubject();
		withExistingChildNode(subject);
		assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canGetChildNode_StringProjectionAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockAction();
		val subject = createSubject();
		withExistingChildNode(subject); // can't really assert that invoking the node method the same node is used
		assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, action));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionAction")
	default void canGetChildNode_StringProjectionBiAction(NodeMethods.IdentityProjectionAction method) {
		val action = mockBiConsumer();
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, action));
		assertThat("calls back with existing child node", action, calledOnceWith(firstArgumentOf(childNode)));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames}]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canGetChildNode_StringProjectionClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(KnownDomainObject.class);
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		val knownObject = assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, closure));
		assertThat("calls back with existing child node",
			closure, calledOnceWith(allOf(singleArgumentOf(knownObject), delegateOf(childNode), delegateFirstStrategy())));
	}

	@ParameterizedTest(name = "can get child node [{argumentsWithNames} - BiClosure]")
	@MethodSource("dev.nokee.model.dsl.NodeParams#stringProjectionClosure")
	default void canGetChildNode_StringProjectionBiClosure(NodeMethods.IdentityProjectionClosure method) {
		val closure = mockClosure(ModelNode.class, KnownDomainObject.class);
		val subject = createSubject();
		val childNode = withExistingChildNode(subject);
		assertDoesNotThrow(() -> method.invoke(subject, "test", TestProjection.class, closure));
		assertThat("calls back with existing child node",
			closure, calledOnceWith(allOf(firstArgumentOf(childNode), delegateOf(childNode), delegateFirstStrategy())));
	}
}
