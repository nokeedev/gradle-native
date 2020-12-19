package dev.nokee.internal;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.alwaysThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(Factories.class)
class Factories_ThrowingTest {
	@Test
	void alwaysThrowException() {
		assertThrows(UnsupportedOperationException.class, () -> alwaysThrow().create());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(alwaysThrow(), alwaysThrow())
			.addEqualityGroup((Factory<?>)() -> null)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(alwaysThrow(), hasToString("Factories.alwaysThrow()"));
	}
}
