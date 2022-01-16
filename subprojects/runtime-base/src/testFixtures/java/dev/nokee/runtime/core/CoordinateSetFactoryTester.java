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
import org.opentest4j.TestAbortedException;

import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
public interface CoordinateSetFactoryTester {
	<T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type);

	<T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates);

	<T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values);

	@Test
	default void canCreateCoordinateSetFromAllEnumValues() {
		val subject = createSubject(Axis.class);
		assertThat(subject, notNullValue(CoordinateSet.class));
		assertThat(subject, containsInAnyOrder(AXIS.create(Axis.a), AXIS.create(Axis.b), AXIS.create(Axis.c)));
		assertThat(subject.getAxis(), equalTo(AXIS));
	}

	@Test
	default void canCreateCoordinateSetFromAxisAndValues() {
		val subject = createSubject(AXIS, Axis.b, Axis.c);
		assertThat(subject, notNullValue(CoordinateSet.class));
		assertThat(subject, containsInAnyOrder(AXIS.create(Axis.b), AXIS.create(Axis.c)));
		assertThat(subject.getAxis(), equalTo(AXIS));
	}

	@Test
	default void canCreateCoordinateSetFromCoordinates() {
		val subject = createSubject(AXIS.create(Axis.b), AXIS.create(Axis.a));
		assertThat(subject, notNullValue(CoordinateSet.class));
		assertThat(subject, containsInAnyOrder(AXIS.create(Axis.b), AXIS.create(Axis.a)));
		assertThat(subject.getAxis(), equalTo(AXIS));
	}

	@Test
	default void throwsExceptionIfDifferentAxis() {
		try {
			createSubject(xAxis().create(0L), yAxis().create(1L));
		} catch (TestAbortedException ex) {
			throw ex;
		} catch (Throwable e) {
			val ex = assertThrows(IllegalArgumentException.class, () -> { throw e; });
			assertThat(ex.getMessage(), equalTo("all coordinate in a set has to be for the same axis"));
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	default void throwsExceptionWhenNoCoordinates() {
		try {
			createSubject(new Coordinate[0]);
		} catch (TestAbortedException ex) {
			throw ex;
		} catch (Throwable e) {
			val ex = assertThrows(IllegalArgumentException.class, () -> { throw e; });
			assertThat(ex.getMessage(), equalTo("coordinate set cannot be empty"));
		}
	}

	@Test
	default void throwsExceptionWhenNoValues() {
		try {
			createSubject(AXIS, new Axis[0]);
		} catch (TestAbortedException ex) {
			throw ex;
		} catch (Throwable e) {
			val ex = assertThrows(IllegalArgumentException.class, () -> { throw e; });
			assertThat(ex.getMessage(), equalTo("coordinate set cannot be empty"));
		}
	}

	CoordinateAxis<Axis> AXIS = CoordinateAxis.of(Axis.class);
	enum Axis { a, b, c}
}
