package dev.nokee.model.internal.core;

import dev.nokee.model.internal.registry.ManagedModelProjection;
import dev.nokee.model.internal.registry.UnmanagedInstanceModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class NodeRegistrationTest {
	@Test
	void canCreateRegistrationOfManagedType() {
		val registration = NodeRegistration.of("c", MyType.class).scope(path("a.b"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("a.b.c")));
			assertThat(registration.getDefaultProjectionType(), equalTo(ModelType.of(MyType.class)));
			assertThat(registration.getProjections(), contains(ManagedModelProjection.of(MyType.class)));
		});
	}

	@Test
	void whenNodeRegistrationAreScopedTheyAreEqualToAnEquivalentModelRegistration() {
		assertThat(NodeRegistration.of("a", MyType.class).scope(path("foo")), equalTo(ModelRegistration.of("foo.a", MyType.class)));
		assertThat(NodeRegistration.of("b", MyType.class).withProjection(UnmanagedInstanceModelProjection.of("bar")).scope(path("bar")),
			equalTo(ModelRegistration.builder().withPath(path("bar.b")).withDefaultProjectionType(ModelType.of(MyType.class)).withProjection(ManagedModelProjection.of(MyType.class)).withProjection(UnmanagedInstanceModelProjection.of("bar")).build()));
	}

	@Test
	void canAddProjection() {
		val registration = NodeRegistration.of("c", MyType.class).withProjection(UnmanagedInstanceModelProjection.of("foo")).scope(path("ab"));
		assertAll(() -> {
			assertThat(registration.getPath(), equalTo(path("ab.c")));
			assertThat(registration.getDefaultProjectionType(), equalTo(ModelType.of(MyType.class)));
			assertThat(registration.getProjections(), contains(ManagedModelProjection.of(MyType.class), UnmanagedInstanceModelProjection.of("foo")));
		});
	}

	// TODO: Add equal to model registration

	interface MyType {}
}
