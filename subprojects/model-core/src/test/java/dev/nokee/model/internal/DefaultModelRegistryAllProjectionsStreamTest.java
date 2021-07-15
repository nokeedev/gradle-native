package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionBuilderAction;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.model.streams.ModelStreamTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class DefaultModelRegistryAllProjectionsStreamTest implements ModelStreamTester<ModelProjection> {
	private final ModelRegistry registry = new DefaultModelRegistry(objectFactory());

	@Override
	public ModelStream<ModelProjection> createSubject() {
		return registry.allProjections();
	}

	@Override
	public ModelProjection createElement() {
		return registry.getRoot().newProjection(builder -> builder.type(Object.class).forInstance(new Object()));
	}

	@Test
	void includesChildNodesProjections() {
		val childNode = registry.getRoot().newChildNode("a");
		val childProjectionA = childNode.newProjection(projection("a"));
		val childProjectionB = childNode.newProjection(projection("b"));
		val grandchildNode = childNode.newChildNode("a");
		val grandChildProjectionC = grandchildNode.newProjection(projection("c"));
		val grandChildProjectionD = grandchildNode.newProjection(projection("d"));
		assertThat(registry.allProjections().collect(toList()),
			providerOf(contains(childProjectionA, childProjectionB, grandChildProjectionC, grandChildProjectionD)));
	}

	private static <S> ModelProjectionBuilderAction<TestProjection> projection(String name) {
		return builder -> builder.forInstance(new TestProjection(name));
	}
}
