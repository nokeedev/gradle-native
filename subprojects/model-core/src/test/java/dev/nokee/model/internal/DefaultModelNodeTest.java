package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultModelNodeTest {
	private final Graph graph = Graph.builder().build();
	private final ModelNode subject = new DefaultModelNode(graph, graph.createNode());

	@Test
	void canCreateChildNode() {
		val childNode = subject.newChildNode("foo");
		assertAll(
			() -> assertThat(childNode, notNullValue()),
			() -> assertThat(childNode, named("foo")),
			() -> assertThat(subject.getChildNodes().collect(toList()), contains(childNode))
		);
	}

	@Test
	void useIdentityToStringWhenNoNamedForChildNodeName() {
		val childNode = subject.newChildNode(new TestIdentity());
		assertAll(
			() -> assertThat(childNode, notNullValue()),
			() -> assertThat(childNode, named("bar")),
			() -> assertThat(subject.getChildNodes().collect(toList()), contains(childNode))
		);
	}

	@Test
	void useIdentityNameForChildNodeName() {
		val childNode = subject.newChildNode(new NamedTestIdentity());
		assertAll(
			() -> assertThat(childNode, notNullValue()),
			() -> assertThat(childNode, named("far")),
			() -> assertThat(subject.getChildNodes().collect(toList()), contains(childNode))
		);
	}

	@Test
	void canCreateProjection() {
		val instance = new TestProjection();
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
		val provider = rootProject().getTasks().register("foo", it -> new RuntimeException());
		assertThrows(RuntimeException.class, () -> subject.newProjection(builder -> builder.type(TestProjection.class).forProvider(provider)));
	}

	@Test
	void hasParentModelNodeReference() {
		assertThat(subject.newChildNode("foo").getParent(), optionalWithValue(is(subject)));
	}

	public static class TestProjection {

	}

	public static class UnknownProjection {}

	public static class TestIdentity {
		@Override
		public String toString() {
			return "bar";
		}
	}

	public static class NamedTestIdentity implements Named {
		@Override
		public String getName() {
			return "far";
		}
	}
}
