package dev.nokee.model;

import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.internal.DefaultModelFactory;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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

	@DisplayName("test projection")
	final class DisplayNameAnnotatedTestProjection {}
	final class DefaultTestProjection {}
}
