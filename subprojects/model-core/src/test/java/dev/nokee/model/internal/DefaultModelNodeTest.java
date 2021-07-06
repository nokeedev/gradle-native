package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.UnknownProjection;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelNodeTester;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertThrows(RuntimeException.class, () -> subject.newProjection(builder -> builder.type(TestProjection.class).forProvider(provider)));
	}
}
