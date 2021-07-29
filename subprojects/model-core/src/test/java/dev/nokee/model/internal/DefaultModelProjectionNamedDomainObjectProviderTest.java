package dev.nokee.model.internal;

import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.core.TypeAwareModelProjectionTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelProjectionNamedDomainObjectProviderTest implements TypeAwareModelProjectionTester {
	@Override
	public TypeAwareModelProjection<TestProjection> createSubject() {
		val container = objectFactory().domainObjectContainer(TestProjection.class, DefaultTestProjection::new);
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		return node.newProjection(builder -> builder.type(TestProjection.class).forProvider(container.register("test")));
	}
}
