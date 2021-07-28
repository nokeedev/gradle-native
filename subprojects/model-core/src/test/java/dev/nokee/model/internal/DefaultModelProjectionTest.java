package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionTester;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.FunctionalInterfaceMatchers;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.FunctionalInterfaceMatchers.singleArgumentOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class DefaultModelProjectionTest implements ModelProjectionTester {
	private final Graph graph = Graph.builder().build();

	@Override
	public ModelProjection createSubject() {
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		return node.newProjection(builder -> builder.type(TestProjection.class).forInstance(new DefaultTestProjection()));
	}

	@Test
	void canRealizeProjectionForNamedProvider() {
		val action = ActionTestUtils.mockAction();
		val container = objectFactory().domainObjectContainer(dev.nokee.model.TestProjection.class);
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		val subject = node.newProjection(builder -> builder.forProvider(container.register("test", action)));
		subject.realize();
		assertThat(action, FunctionalInterfaceMatchers.calledOnceWith(singleArgumentOf(isA(dev.nokee.model.TestProjection.class))));
	}
}
