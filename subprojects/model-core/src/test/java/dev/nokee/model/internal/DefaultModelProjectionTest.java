package dev.nokee.model.internal;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionTester;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
		assertThat(action, calledOnceWith(singleArgumentOf(isA(dev.nokee.model.TestProjection.class))));
	}

	@Test
	void canGetProjectionAsProviderWithoutRealizing() {
		val action = ActionTestUtils.mockAction();
		val container = objectFactory().domainObjectContainer(dev.nokee.model.TestProjection.class);
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		val subject = node.newProjection(builder -> builder.forProvider(container.register("test", action)));
		val provider = subject.get(new TypeOf<NamedDomainObjectProvider<dev.nokee.model.TestProjection>>() {}.getConcreteClass());
		assertThat(provider, isA(Provider.class));
		assertThat(ProviderUtils.getType(provider), optionalWithValue(is(dev.nokee.model.TestProjection.class)));
		assertThat(action, neverCalled());
	}

	@Test
	void canGetProjectionAsNamedProviderWithoutRealizing() {
		val action = ActionTestUtils.mockAction();
		val container = objectFactory().domainObjectContainer(dev.nokee.model.TestProjection.class);
		val node = new DefaultModelFactory(graph).createNode(graph.createNode());
		val subject = node.newProjection(builder -> builder.forProvider(container.register("test", action)));
		val provider = subject.get(new TypeOf<NamedDomainObjectProvider<dev.nokee.model.TestProjection>>() {}.getConcreteClass());
		assertThat(provider, isA(NamedDomainObjectProvider.class));
		assertThat(ProviderUtils.getType(provider), optionalWithValue(is(dev.nokee.model.TestProjection.class)));
		assertThat(action, neverCalled());
	}
}
