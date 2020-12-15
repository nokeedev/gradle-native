package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.core.ModelActions.doNothing;
import static dev.nokee.model.internal.core.ModelActions.onlyIf;
import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelPath.path;
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
			assertThat(registration.getProjections(), contains(ModelProjections.managed(of(MyType.class))));
			assertThat(registration.getActions(), empty());
		});
	}

	@Test
	void canCreateRegistrationOfUnmanagedType() {
		val registration = NodeRegistration.unmanaged("z", of(MyType.class), alwaysThrow()).scope(path("x.y"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("x.y.z")));
			assertThat(registration.getDefaultProjectionType(), equalTo(of(MyType.class)));
			assertThat(registration.getProjections(), contains(ModelProjections.createdUsing(of(MyType.class), alwaysThrow())));
			assertThat(registration.getActions(), empty());
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
			assertThat(registration.getProjections(), contains(ModelProjections.managed(of(MyType.class)), ModelProjections.ofInstance("foo")));
			assertThat(registration.getActions(), empty());
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
			.addEqualityGroup(NodeRegistration.of("a", of(MyType.class)).withProjection(ModelProjections.ofInstance("foo")).action(stateAtLeast(ModelNode.State.Registered), doNothing()))
			.testEquals();
	}

	@Test
	void canAddActions() {
		val registration = NodeRegistration.of("bar", of(MyType.class)).action(stateAtLeast(ModelNode.State.Registered), doNothing()).scope(path("foo"));
		assertThat(registration.getActions(), contains(onlyIf(stateAtLeast(ModelNode.State.Registered), doNothing())));
	}

	interface MyType {}
	interface MyOtherType {}
}
