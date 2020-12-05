package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.utils.TestUtils;
import lombok.val;
import org.gradle.api.plugins.ExtensionAware;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.inject;
import static dev.nokee.model.internal.core.ModelNodes.of;
import static dev.nokee.model.internal.core.ModelPath.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		val node = new ModelNode(path("a.b.c"));
		assertEquals(node, of(inject(undecoratedObject(), node)), "should be able to inject model node in ExtensibleAware types");
	}

	private static Object decoratedObjectWithModelNode() {
		val node = new ModelNode(path("a.b.c"));
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
}
