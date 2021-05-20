package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;

class DefaultModelProjectionTest implements ModelProjectionTester {
	private final Graph graph = Graph.builder().build();

	@Override
	public ModelProjection createSubject() {
		val node = new DefaultModelNode(graph, graph.createNode());
		return node.newProjection(builder -> builder.type(TestProjection.class).forInstance(new DefaultTestProjection()));
	}
}
