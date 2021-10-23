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

import static org.junit.jupiter.api.Assertions.*;

public class CoordinateAxisCreateTest {
	@Test
	void returnsTheSameInstanceWhenCoordinateValueIsACoordinateItselfOfTheExactAxis() {
		val value = new MyCustomCoordinateOfDefaultMyValueAxis();
		assertSame(CoordinateAxis.of(MyValue.class).create(value), value);
	}

	@Test
	void returnsADifferentInstanceWhenCoordinateValueIsACoordinateItselfOfAnotherAxis() {
		val value = new MyCustomCoordinateOfCustomMyValueAxis();
		assertNotSame(CoordinateAxis.of(MyValue.class).create(value), value);
	}

	private interface MyValue {}

	private static final class MyCustomCoordinateOfDefaultMyValueAxis implements MyValue, Coordinate<MyValue> {}
	private static final class MyCustomCoordinateOfCustomMyValueAxis implements MyValue, Coordinate<MyValue> {
		@Override
		public CoordinateAxis<MyValue> getAxis() {
			return CoordinateAxis.of(MyValue.class, "something-else");
		}
	}
}
