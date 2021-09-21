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
