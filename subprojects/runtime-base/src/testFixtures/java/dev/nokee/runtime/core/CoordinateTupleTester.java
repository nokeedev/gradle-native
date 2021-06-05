package dev.nokee.runtime.core;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface CoordinateTupleTester<T extends CoordinateTuple> {
	T createSubject();

	@Test
	default void hasCoordinates() {
		// Empty tuple are meaningless, always expect at least one coordinate
		assertThat(createSubject(), iterableWithSize(greaterThan(0)));
	}

	@Test
	default void canAccessEachCoordinates() {
		val subject = createSubject();
		subject.forEach(coordinate -> {
			assertThat(subject.get(coordinate.getAxis()), is(coordinate.getValue()));
		});
	}

	@Test
	default void throwsExceptionIfAxisIsNotKnown() {
		val ex = assertThrows(IllegalArgumentException.class,
			() -> createSubject().get(CoordinateAxis.of(UnknownAxis.class, "unknown-axis")));
		assertThat(ex.getMessage(), equalTo("No coordinate exists for axis <unknown-axis>"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	interface UnknownAxis {}
}
