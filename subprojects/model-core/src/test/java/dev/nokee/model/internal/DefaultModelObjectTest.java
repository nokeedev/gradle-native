package dev.nokee.model.internal;

import dev.nokee.model.BaseProjection;
import dev.nokee.model.TestProjection;
import dev.nokee.model.core.ModelObject;
import dev.nokee.model.core.ModelObjectIntegrationTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class DefaultModelObjectTest extends ModelObjectIntegrationTester<TestProjection> {
	private final TestProjection object = new TestProjection("test");
	private ModelObject<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val factory = new DefaultModelFactory(graph, objectFactory(), null);
		val node = factory.createNode(graph.createNode());
		val projection = node.newProjection(builder -> builder.type(TestProjection.class).forInstance(object));
		subject = factory.createObject(projection);
	}

	@Override
	public ModelObject<TestProjection> subject() {
		return subject;
	}

	@Test
	void isCallableImplementationReturningModelObjectAsProvider() throws Exception {
		assertThat(subject(), isA(Callable.class));
		assertThat(((Callable<Object>) subject()).call(), isA(Provider.class));
		assertThat((Provider<Object>) ((Callable<Object>) subject()).call(), providerOf(object));
	}

	@Test
	void returnsTrueForBaseTypeInstanceTest() {
		assertThat(subject().instanceOf(BaseProjection.class), is(true));
	}
}
