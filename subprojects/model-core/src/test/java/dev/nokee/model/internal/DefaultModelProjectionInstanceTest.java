package dev.nokee.model.internal;

import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.core.TypeAwareModelProjectionTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;

class DefaultModelProjectionInstanceTest implements TypeAwareModelProjectionTester {
	@Override
	public TypeAwareModelProjection<TestProjection> createSubject() {
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		return node.newProjection(builder -> builder.type(TestProjection.class).forInstance(new DefaultTestProjection("test")));
	}
}
