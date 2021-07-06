package dev.nokee.model.core;

import dev.nokee.model.TestProjection;
import dev.nokee.model.UnknownProjection;
import lombok.val;
import org.gradle.api.Named;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface ModelNodeTester {
	ModelNode createSubject();

	@Test
	default void childNodeHasParentNodeReference() {
		val parent = createSubject();
		assertThat(parent.newChildNode("test").getParent(), optionalWithValue(equalTo(parent)));
	}

	@Test
	default void canCreateChildNode() {
		val childNode = createSubject().newChildNode("test");
		assertThat(childNode, notNullValue());
		assertThat(childNode, named("test"));
	}

	@Test
	default void childNodesAreAccessibleFromParent() {
		val subject = createSubject();
		val childNode0 = subject.newChildNode("test0");
		val childNode1 = subject.newChildNode("test1");
		val childNode2 = subject.newChildNode("test2");
		assertThat(subject.getChildNodes().collect(toList()), contains(childNode0, childNode1, childNode2));
	}

	@Test
	default void noChildNodesOnNewNodes() {
		assertThat(createSubject().getChildNodes().count(), is(0L));
	}

	@Test
	default void noProjectionsOnNewNodes() {
		assertThat(createSubject().getProjections().count(), is(0L));
	}

	@Test
	default void useIdentityToStringWhenNoNamedForChildNodeName() {
		val subject = createSubject();
		val childNode = subject.newChildNode(new TestIdentity("bar"));
		assertThat(childNode, named("bar"));
	}

	class TestIdentity {
		private final String name;

		public TestIdentity(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Test
	default void useIdentityNameForChildNodeName() {
		val subject = createSubject();
		val childNode = subject.newChildNode(new NamedTestIdentity("far"));
		assertThat(childNode, named("far"));
	}

	class NamedTestIdentity implements Named {
		private final String name;

		public NamedTestIdentity(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

	@Test
	default void canViewNodeAsProjection() {
		val subject = createSubject();
		subject.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		assertAll(
			() -> assertThat(subject.canBeViewedAs(TestProjection.class), is(true)),
			() -> assertThat(subject.canBeViewedAs(UnknownProjection.class), is(false))
		);
	}

	@Test
	default void newProjectionHasCurrentNodeAsOwner() {
		val subject = createSubject();
		val projection = subject.newProjection(builder -> builder.type(TestProjection.class).forInstance(new TestProjection("test")));
		assertThat(projection.getOwner(), is(subject));
	}

	@Test
	default void inferTypeFromInstanceWhenCreatingProjectionByInstanceOnly() {
		assertThat(createSubject().newProjection(builder -> builder.forInstance(new TestProjection("test"))).canBeViewedAs(TestProjection.class), is(true));
	}

	@Test
	default void inferTypeFromProviderWhenCreatingProjectionByProvider() {
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		assertThat(createSubject().newProjection(builder -> builder.forProvider(container.register("test"))).canBeViewedAs(TestProjection.class), is(true));
	}
}
