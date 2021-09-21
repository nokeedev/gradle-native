/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
