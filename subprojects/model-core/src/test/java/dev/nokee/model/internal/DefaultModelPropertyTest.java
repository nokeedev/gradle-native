package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelObjectIntegrationTester;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.ModelPropertyTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelPropertyTest extends ModelObjectIntegrationTester<TestProjection> implements ModelPropertyTester<TestProjection> {
	private ModelObject<?> parent;
	private ModelProperty<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val factory = new DefaultModelFactory(graph, objectFactory(), null);
		val parentNode = factory.createNode(graph.createNode());
		val parentProjection = parentNode.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		parent = factory.createObject(parentProjection);

		val node = parentNode.newChildNode("gery");
		val projection = parentNode.newProjection(builder -> builder.forInstance(new TestProjection("gery")));
		subject = new DefaultModelProperty<>(projection);
	}

	@Override
	public ModelProperty<TestProjection> subject() {
		return subject;
	}
}
