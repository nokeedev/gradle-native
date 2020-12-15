package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.inject;
import static dev.nokee.model.internal.core.ModelNodes.of;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

public class ModelNodesTest {
	@Test
	void canAccessModelNodeFromExtensibleAware() {
		assertThat("model node is accessible on decorated objects", of(decoratedExtensionAwareObjectWithModelNode()), isA(ModelNode.class));
	}

	@Test
	void canAccessModelNodeFromModelNodeAware() {
		assertThat("model node is accessible on decorated objects", of(decoratedModelNodeAwareObjectWithModelNode()), isA(ModelNode.class));
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

	private static Object decoratedExtensionAwareObjectWithModelNode() {
		val node = node("a.b.c");
		val object = TestUtils.objectFactory().newInstance(MyType.class);
		((ExtensionAware) object).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		return object;
	}

	private static Object decoratedModelNodeAwareObjectWithModelNode() {
		val node = node("a.b.c");
		val object = TestUtils.objectFactory().newInstance(MyType.class);
		((ExtensionAware) object).getExtensions().add(ModelNode.class, "__NOKEE_modelNode", node);
		return new ModelNodeAware() {
			@Override
			public ModelNode getNode() {
				return node;
			}
		};
	}

	private static Object object() {
		return new Object();
	}

	private static Object undecoratedObject() {
		return TestUtils.objectFactory().newInstance(MyType.class);
	}

	interface MyType {}
}
