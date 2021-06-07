package dev.nokee.runtime.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public interface CoordinateSpaceFactoryTester {
	CoordinateSpace createSubject(CoordinateSet<?>... coordinateSets);

	@Test
	default void canCreateSpaceFromTheCartesianProductOfAllCoordinateSet() {
		val subject = createSubject(
			CoordinateSet.of(xAxis().create(0L), xAxis().create(1L), xAxis().create(2L)),
			CoordinateSet.of(yAxis().create(0L), yAxis().create(1L), yAxis().create(2L)));
		assertThat(subject, containsInAnyOrder(
			point(0, 0), point(0, 1), point(0, 2),
			point(1, 0), point(1, 1), point(1, 2),
			point(2, 0), point(2, 1), point(2, 2)));
	}
}
