package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

public class ModelNodesTest {
	@Test
	void canAccessModelNodeFromExtensibleAware() {
		assertThat("model node is accessible on decorated objects", of(decoratedObjectWithModelNode()), isA(ModelNode.class));
	}

	@Test
	void throwsExceptionIfObjectIsNotExtensibleAware() {
		assertThrows(IllegalArgumentException.class, () -> of(object()), "cannot get model node for non-ExtensibleAware types");
	}

	@Test
	void throwsExceptionIfObjectIsNotDecoratedWithModelNode() {
		assertThrows(IllegalArgumentException.class, () -> of(undecoratedObject()), "cannot get model node if not present");
	}

	@Test
	void canInjectModelNode() {
		val node = node("a.b.c");
		assertEquals(node, of(inject(undecoratedObject(), node)), "should be able to inject model node in ExtensibleAware types");
	}

	@Test
	void canCreatePredicateFilterForModelNodeByType() {
		val predicate = withType(ModelType.of(MyType.class));
		assertTrue(predicate.test(node(projectionOf(MyType.class))));
		assertFalse(predicate.test(node(projectionOf(WrongType.class))));
	}

	@Test
	void canCreatePredicateFilterForModelNodeByState() {
		val predicate = ModelNodes.stateAtLeast(ModelNode.State.Registered);
		assertFalse(predicate.test(node()));
		assertTrue(predicate.test(node().register()));
		assertTrue(predicate.test(node().realize()));
	}

	private static Object decoratedObjectWithModelNode() {
		val node = node("a.b.c");
		val object = TestUtils.objectFactory().newInstance(MyType.class);
		((ExtensionAware) object).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		return object;
	}

	private static Object object() {
		return new Object();
	}

	private static Object undecoratedObject() {
		return TestUtils.objectFactory().newInstance(MyType.class);
	}

	interface MyType {}
	interface WrongType {}
}
