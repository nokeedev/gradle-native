package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface CoordinateTupleFactoryTester {
	default CoordinateTuple createEmptySubject() {
		return createSubject();
	}

	CoordinateTuple createSubject(Coordinate<?>... coordinates);

	@Test
	default void throwsExceptionWhenNoCoordinates() {
		val ex = assertThrows(IllegalArgumentException.class, this::createEmptySubject);
		assertThat(ex.getMessage(), equalTo("coordinates cannot be empty"));
	}

	@Test
	default void throwsExceptionWhenDuplicatedAxis() {
		val ex = assertThrows(IllegalArgumentException.class, () -> createSubject(xAxis().create(4L), xAxis().create(2L)));
		assertThat(ex.getMessage(), equalTo("coordinates cannot contains duplicated axis"));
	}

	@Test
	default void canCreateCoordinateTuple() {
		val subject = createSubject(xAxis().create(1L), yAxis().create(2L), zAxis().create(3L));
		assertThat(subject, notNullValue(CoordinateTuple.class));
		assertThat(subject, iterableWithSize(3));
	}

	@Test
	default void throwsExceptionForNullValue() {
		assertAll(
			() -> assertThrows(NullPointerException.class, () -> createSubject((Coordinate<?>[]) null)),
			() -> assertThrows(NullPointerException.class, () -> createSubject(xAxis().create(1L), null))
		);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullConstruction() throws NoSuchMethodException {
		new NullPointerTester()
			.testMethod(this, CoordinateTupleFactoryTester.class.getDeclaredMethod("createSubject", Coordinate[].class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(xAxis().create(1L)), createSubject(xAxis().create(1L)))
			.addEqualityGroup(createSubject(yAxis().create(1L)))
			.addEqualityGroup(createSubject(xAxis().create(2L)))
			.addEqualityGroup(createSubject(xAxis().create(1L), yAxis().create(2L)))
			.addEqualityGroup(createSubject(yAxis().create(2L), xAxis().create(1L)))
			.testEquals();
	}
}
