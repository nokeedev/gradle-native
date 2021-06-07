package dev.nokee.runtime.core;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static dev.nokee.runtime.core.Coordinates.standardBasis;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class CoordinateSpaceStandardBasisTest {
	@Test
	void singlePointHasNoAxis() {
		assertThat(standardBasis(CoordinateSpace.of(point(1))).count(), is(0L));
	}

	@Test
	void multiplePointsOnSameAxisDifferentValues() {
		assertThat(standardBasis(CoordinateSpace.of(point(1), point(2))).collect(toList()), containsInAnyOrder(xAxis()));
	}

	@Test
	void multiplePointsWithDifferentAxisAndValue() {
		assertThat(standardBasis(CoordinateSpace.of(point(1, 0), point(0, 1))).collect(toList()), containsInAnyOrder(xAxis(), yAxis()));
	}

	@Test
	void multiplePointsWithDifferentAxisAndSameValueForOneAxis() {
		assertThat(standardBasis(CoordinateSpace.of(point(1, 0), point(1, 1))).collect(toList()), containsInAnyOrder(yAxis()));
	}
}
