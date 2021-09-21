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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public interface CoordinateFactoryTester {
	<T> Coordinate<T> createSubject(CoordinateAxis<T> axis, T value);

	@Test
	default void checkAxis() {
		assertThat(createSubject(xAxis(), 4L).getAxis(), equalTo(xAxis()));
		assertThat(createSubject(yAxis(), 3L).getAxis(), equalTo(yAxis()));
	}

	@Test
	default void checkValue() {
		assertThat(createSubject(xAxis(), 4L).getValue(), is(4L));
		assertThat(createSubject(yAxis(), 3L).getValue(), is(3L));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullConstruction() throws NoSuchMethodException {
		new NullPointerTester()
			.setDefault(CoordinateAxis.class, testAxis())
			.testMethod(this, CoordinateFactoryTester.class.getDeclaredMethod("createSubject", CoordinateAxis.class, Object.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(xAxis(), 1L), createSubject(xAxis(), 1L))
			.addEqualityGroup(createSubject(yAxis(), 1L))
			.addEqualityGroup(createSubject(xAxis(), 2L))
			.testEquals();
	}
}
