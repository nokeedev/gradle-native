package dev.nokee.model.internal;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelObjectTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;

class DefaultModelObjectTest implements ModelObjectTester<TestProjection> {
	private ModelObject<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		val projection = node.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		subject = new DefaultModelObject<>(projection);
	}

	@Override
	public ModelObject<TestProjection> subject() {
		return subject;
	}
}
