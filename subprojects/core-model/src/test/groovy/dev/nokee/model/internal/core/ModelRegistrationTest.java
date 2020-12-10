package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.Factory;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static dev.nokee.model.internal.core.ModelIdentifier.of;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelRegistration.bridgedInstance;
import static dev.nokee.model.internal.core.ModelRegistration.unmanagedInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ModelRegistrationTest {
	@Test
	void canCreateFromRawPathAndRawType() {
		assertAll(() -> {
			val registration = ModelRegistration.of("a.b.c", MyType.class);
			assertEquals(path("a.b.c"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelIdentifier.class, of("x.y.z", MyType.class)).testAllPublicStaticMethods(ModelRegistration.class);
	}

	@Test
	void canCreateRegistrationForInstance() {
		assertAll(() -> {
			val registration = unmanagedInstance(of("foo", MyType.class), mock(Factory.class));
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());

			// Assume projection of unmanaged instance
			assertThat(registration.getProjections().size(), equalTo(1));
		});
	}

	@Test
	void canCreateRegistrationForDeferredInstance() {
		assertAll(() -> {
			val registration = bridgedInstance(of("foo", MyType.class), new MyType());
			assertEquals(path("foo"), registration.getPath());
			assertEquals(of(MyType.class), registration.getDefaultProjectionType());

			// Assume projection of bridged instance
			assertThat(registration.getProjections().size(), equalTo(1));
		});
	}

	@Test
	void canDerivedRegistrationWithDifferentProjections() {
		val originalRegistration = bridgedInstance(of("foo", MyType.class), new MyType());
		val derivedRegistration = originalRegistration.withProjections(Collections.emptyList());

		assertAll(() -> {
			assertThat("original registration has default projections", originalRegistration.getProjections(), hasSize(1));
			assertThat("derived registration has no projections", derivedRegistration.getProjections(), empty());
			assertThat("original registration is not the same instance than derived registration", originalRegistration, not(equalTo(derivedRegistration)));
		});
	}

	static class MyType {}
}
