package dev.nokee.runtime.core;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
			assertThat(subject.find(coordinate.getAxis()), optionalWithValue(is(coordinate.getValue())));
		});
	}

	@Test
	default void cannotFindUnknownAxis() {
		assertThat(createSubject().find(CoordinateAxis.of(UnknownAxis.class, "unknown-axis")), emptyOptional());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	interface UnknownAxis {}
}
