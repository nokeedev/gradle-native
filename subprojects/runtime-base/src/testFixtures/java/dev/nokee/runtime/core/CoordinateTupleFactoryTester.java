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
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.zAxis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;
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
			.addEqualityGroup(createSubject(xAxis().create(1L), yAxis().create(2L)), createSubject(yAxis().create(2L), xAxis().create(1L)))
			.testEquals();
	}
}
