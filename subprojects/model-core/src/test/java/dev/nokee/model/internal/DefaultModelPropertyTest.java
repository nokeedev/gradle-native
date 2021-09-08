package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelObjectIntegrationTester;
import dev.nokee.model.core.ModelProperty;
import dev.nokee.model.core.ModelPropertyTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class DefaultModelPropertyTest extends ModelObjectIntegrationTester<TestProjection> implements ModelPropertyTester<TestProjection> {
	private final TestProjection object = new TestProjection("gery");
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
		val projection = node.newProjection(builder -> builder.forInstance(object));
		subject = new DefaultModelProperty<>(factory.createObject(projection));
	}

	@Override
	public ModelProperty<TestProjection> subject() {
		return subject;
	}

	@Test
	void isCallableImplementationReturningModelObjectAsProvider() throws Exception {
		assertThat(subject(), isA(Callable.class));
		assertThat(((Callable<Object>) subject()).call(), isA(Provider.class));
		assertThat((Provider<Object>) ((Callable<Object>) subject()).call(), providerOf(object));
	}
}
