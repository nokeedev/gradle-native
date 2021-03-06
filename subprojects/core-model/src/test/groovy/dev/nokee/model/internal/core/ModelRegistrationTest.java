package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelRegistration.*;
import static dev.nokee.model.internal.core.ModelTestActions.doSomething;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelRegistrationTest {
	@Test
	void canCreateFromRawPathAndRawType() {
		assertAll(() -> {
			val registration = ModelRegistration.of("a.b.c", MyType.class);
			assertEquals(path("a.b.c"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), iterableWithSize(1)); // for the projections
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelIdentifier.class, of("x.y.z", MyType.class)).testAllPublicStaticMethods(ModelRegistration.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				ModelRegistration.of("po.ta.to", MyType.class),
				ModelRegistration.of("po.ta.to", MyType.class))
			.addEqualityGroup(ModelRegistration.of("to.ma.to", MyType.class))
			.addEqualityGroup(
				ModelRegistration.of("po.ta.to", MyOtherType.class),
				builder().withPath(path("po.ta.to")).withProjection(ModelProjections.managed(of(MyOtherType.class))).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).build(),
				builder().withPath(path("po.ta.to")).withDefaultProjectionType(of(MyType.class)).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).withProjection(ModelProjections.ofInstance(new MyType())).build())
			.addEqualityGroup(
				builder().withPath(path("po.ta.to")).action(doSomething()).build())
			.testEquals();
	}

	@Test
	void canCreateRegistrationForInstance() {
		assertAll(() -> {
			val registration = unmanagedInstance(of("foo", MyType.class), alwaysThrow());
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), iterableWithSize(1)); // for the projections
		});
	}

	@Test
	void canCreateRegistrationForDeferredInstance() {
		assertAll(() -> {
			val registration = bridgedInstance(of("foo", MyType.class), new MyType());
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
			assertThat(registration.getActions(), iterableWithSize(1)); // for the projections
		});
	}

	@Test
	void canAddActionToRegistration() {
		assertAll(() -> {
			val registration = ModelRegistration.builder()
				.withPath(path("a.b.c"))
				.withDefaultProjectionType(of(MyType.class))
				.action(doSomething())
				.build();
			assertThat(registration.getActions(), hasItem(doSomething())); // other are projections
		});
	}

	static class MyType {}
	static class MyOtherType {}
}
