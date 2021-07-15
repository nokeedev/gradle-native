package dev.nokee.model;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.internal.DefaultModelFactory;
import dev.nokee.model.internal.DomainObjectIdentities;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.DomainObjectIdentities.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;

public interface DomainObjectIdentifierTester {
	DomainObjectIdentifier createSubject(ModelProjection projection);

	@Test
	default void composeDisplayNameFromAnnotationWhenPresent() {
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode()).newChildNode("foo");
		val projection = node.newProjection(builder -> builder.forInstance(new DisplayNameAnnotatedTestProjection()));
		assertThat(createSubject(projection), hasToString("test projection 'foo'"));
	}

	@Test
	default void infersDisplayNameFromTypeName() {
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode()).newChildNode("bar");
		val projection = node.newProjection(builder -> builder.forInstance(new DefaultTestProjection()));
		assertThat(createSubject(projection), hasToString("default test projection 'bar'"));
	}

	@Test
	default void canIterateNodeIdentity() {
		val graph = Graph.builder().build();
		val node = new DefaultModelFactory(graph).createNode(graph.createNode()).newChildNode("foo").newChildNode("bar");
		val projection = node.newProjection(builder -> builder.forInstance(new DefaultTestProjection()));
		assertThat(createSubject(projection), contains(root(), "foo", "bar"));
	}

	@DisplayName("test projection")
	final class DisplayNameAnnotatedTestProjection {}
	final class DefaultTestProjection {}
}
