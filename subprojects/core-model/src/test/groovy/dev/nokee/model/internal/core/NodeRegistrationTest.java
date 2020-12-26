package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@Subject(NodeRegistration.class)
class NodeRegistrationTest {
	@Test
	void canCreateRegistrationOfManagedType() {
		val registration = NodeRegistration.of("c", of(MyType.class)).scope(path("a.b"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("a.b.c")));
			assertThat(registration.getDefaultProjectionType(), equalTo(of(MyType.class)));
			assertThat(registration.getActions(), iterableWithSize(1)); // for projections
		});
	}

	@Test
	void canCreateRegistrationOfUnmanagedType() {
		val registration = NodeRegistration.unmanaged("z", of(MyType.class), alwaysThrow()).scope(path("x.y"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("x.y.z")));
			assertThat(registration.getDefaultProjectionType(), equalTo(of(MyType.class)));
			assertThat(registration.getActions(), iterableWithSize(1)); // for projections
		});
	}

	@Test
	void whenNodeRegistrationAreScopedTheyAreEqualToAnEquivalentModelRegistration() {
		assertThat(NodeRegistration.of("a", of(MyType.class)).scope(path("foo")), equalTo(ModelRegistration.of("foo.a", MyType.class)));
		assertThat(NodeRegistration.of("b", of(MyType.class)).withProjection(ModelProjections.ofInstance("bar")).scope(path("bar")),
			equalTo(ModelRegistration.builder().withPath(path("bar.b")).withDefaultProjectionType(of(MyType.class)).withProjection(ModelProjections.managed(of(MyType.class))).withProjection(ModelProjections.ofInstance("bar")).build()));
	}

	@Test
	void canAddProjection() {
		val registration = NodeRegistration.of("c", of(MyType.class)).withProjection(ModelProjections.ofInstance("foo")).scope(path("ab"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("ab.c")));
			assertThat(registration.getDefaultProjectionType(), equalTo(of(MyType.class)));
			assertThat(registration.getActions(), iterableWithSize(1)); // for projections
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)), NodeRegistration.of("a", of(MyType.class)))
			.addEqualityGroup(NodeRegistration.of("c", of(MyType.class)))
			.addEqualityGroup(NodeRegistration.of("a", of(MyOtherType.class)))
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)).withProjection(ModelProjections.ofInstance("foo")))
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)).withProjection(ModelProjections.ofInstance("foo")).action(self(stateAtLeast(ModelNode.State.Registered)).apply(doSomething())))
			.testEquals();
	}

	@Test
	void canAddActions() {
		val registration = NodeRegistration.of("bar", of(MyType.class)).action(self(stateAtLeast(ModelNode.State.Registered)).apply(doSomething())).scope(path("foo"));
		assertThat(registration.getActions(), hasItem(matching(self(stateAtLeast(ModelNode.State.Registered)).scope(path("foo.bar")), doSomething()))); // other are for projections
	}

	@Test
	void canAddActionsUsingNodePredicate() {
		val registration = NodeRegistration.of("b", of(MyType.class))
			.action(allDirectDescendants().apply(doSomething())).scope(path("a"));
		assertThat(registration.getActions(), hasItem(matching(allDirectDescendants().scope(path("a.b")), doSomething()))); // other are for projections
	}

	interface MyType {}
	interface MyOtherType {}
}
