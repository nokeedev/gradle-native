package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.ModelPropertyTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;

class DefaultModelPropertyTest implements ModelPropertyTester<TestProjection> {
	private ModelObject<?> parent;
	private ModelProperty<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val parentNode = new DefaultModelFactory(graph).createNode(graph.createNode());
		val parentProjection = parentNode.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		parent = new DefaultModelObject<>(parentProjection);

		val node = parentNode.newChildNode("gery");
		val projection = parentNode.newProjection(builder -> builder.forInstance(new TestProjection("gery")));
		subject = new DefaultModelProperty<>(projection);
	}

	@Override
	public ModelProperty<TestProjection> subject() {
		return subject;
	}
}
