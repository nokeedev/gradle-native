package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.UnknownProjection;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelNodeTester;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Optional;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class DefaultModelNodeTest implements ModelNodeTester {
	private final Graph graph = Graph.builder().build();

	@Override
	public ModelNode createSubject() {
		return new DefaultModelFactory(graph).createNode(graph.createNode());
	}

	@Test
	void canCreateProjectionForInstance() {
		val subject = createSubject();
		val instance = new TestProjection("test");
		val projection = subject.newProjection(builder -> builder.type(TestProjection.class).forInstance(instance));
		assertAll(
			() -> assertThat(projection, notNullValue()),
			() -> assertThat(projection.canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(projection.canBeViewedAs(UnknownProjection.class), is(false)),
			() -> assertThat(projection.get(TestProjection.class), is(instance)),
			() -> assertThat(subject.getProjections().collect(toList()), contains(projection))
		);
	}

	@Test
	void doesNotRealizeProjectionProvider() {
		val subject = createSubject();
		val provider = rootProject().getTasks().register("foo", it -> new RuntimeException());
		val projection = subject.newProjection(builder -> builder.type(Task.class).forProvider(provider));
		assertAll(
			() -> assertThat(projection, notNullValue()),
			() -> assertThat(projection.canBeViewedAs(Task.class), is(true)),
			() -> assertThat(projection.canBeViewedAs(UnknownProjection.class), is(false)),
			() -> assertThat(subject.getProjections().collect(toList()), contains(projection))
		);
	}

	@Test
	void throwsExceptionWhenProjectionTypeDoesNotMatchProviderTypeIfAvailable() {
		val subject = createSubject();
		val provider = rootProject().getTasks().register("foo", it -> new RuntimeException());
		assertThrows(RuntimeException.class, () -> subject.newProjection(builder -> builder.type(TestProjection.class).forProvider(Cast.uncheckedCast("forcing type erasure scenario", provider))));
	}

	@Test
	void doesNotAllowCreatingChildNodeUsingRootIdentity() {
		val ex = assertThrows(IllegalArgumentException.class, () -> createSubject().newChildNode(DomainObjectIdentities.root()));
		assertThat(ex.getMessage(), equalTo("Cannot use known root identity as child node identity."));
	}

	@Test
	void canCreateProjectionByRegisteringElementInContainerByType() {
		val container = objectFactory().domainObjectContainer(TestProjection.class, TestProjection::new);
		val registry = new DefaultNamedDomainObjectRegistry().registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		val modelRegistry = new DefaultModelRegistry(objectFactory(), registry);
		assertDoesNotThrow(() -> modelRegistry.getRoot().newChildNode("foo").newChildNode("bar").newProjection(builder -> builder.type(TestProjection.class)));
		assertThat(container.findByName("fooBar"), isA(TestProjection.class));
	}

	@Test
	void canCreateTrivialProjectionUsingObjectFactoryByType() {
		val modelRegistry = new DefaultModelRegistry(objectFactory());
		val projection = assertDoesNotThrow(() -> modelRegistry.getRoot().newChildNode("foo").newChildNode("bar").newProjection(builder -> builder.type(ITestProjection.class)));
		assertThat(projection.get(), isA(ITestProjection.class));
	}

	@Test
	void canCreateProjectionByIgnoringNameProviderNode() {
		val container = objectFactory().domainObjectContainer(TestProjection.class, TestProjection::new);
		val registry = new DefaultNamedDomainObjectRegistry().registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		val modelRegistry = new DefaultModelRegistry(objectFactory(), registry);
		assertDoesNotThrow(() -> modelRegistry.getRoot().newChildNode(named("foo", null)).newChildNode(named("b", "bar")).newChildNode("far").newProjection(builder -> builder.type(TestProjection.class)));
		assertThat(container.findByName("barFar"), isA(TestProjection.class));
	}

	static NameProvider named(String toString, @Nullable String name) {
		return new NameProvider() {
			@Override
			public Optional<String> getProvidedName() {
				return Optional.ofNullable(name);
			}

			@Override
			public String toString() {
				return toString;
			}
		};
	}
}
