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

import com.google.common.testing.EqualsTester;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.testAxis;
import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static dev.nokee.runtime.core.Coordinates.isAbsentCoordinate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AbsentCoordinateTest {
	private final Coordinate<TestAxis> subject = absentCoordinate(testAxis());

	@Test
	void hasAxis() {
		assertEquals(testAxis(), subject.getAxis());
	}

	@Test
	void throwsExceptionWhenGettingTheValue() {
		assertThrows(UnsupportedOperationException.class, () -> subject.getValue());
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("absent value for axis <test>"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subject, absentCoordinate(testAxis()))
			.addEqualityGroup(absentCoordinate(CoordinateAxis.of(TestAxis.class, "ldkr")))
			.addEqualityGroup(testAxis().create(new TestAxis()))
			.testEquals();
	}

	@Test
	void returnsTrueOnAbsentCoordinateCheck() {
		assertTrue(isAbsentCoordinate(subject));
	}
}
