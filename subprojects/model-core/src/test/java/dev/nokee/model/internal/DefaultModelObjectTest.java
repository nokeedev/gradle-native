package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelObjectIntegrationTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelObjectTest extends ModelObjectIntegrationTester<TestProjection> {
	private ModelObject<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val factory = new DefaultModelFactory(graph, objectFactory(), null);
		val node = factory.createNode(graph.createNode());
		val projection = node.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		subject = factory.createObject(projection);
	}

	@Override
	public ModelObject<TestProjection> subject() {
		return subject;
	}
}
