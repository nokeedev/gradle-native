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

import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class CoordinateTestUtils {
	public static CoordinateAxis<TestAxis> testAxis() {
		return CoordinateAxis.of(TestAxis.class, "test");
	}

	public static CoordinateAxis<Long> xAxis() {
		return CoordinateAxis.of(Long.class, "x");
	}

	public static CoordinateAxis<Long> yAxis() {
		return CoordinateAxis.of(Long.class, "y");
	}

	public static CoordinateAxis<Long> zAxis() {
		return CoordinateAxis.of(Long.class, "z");
	}

	public static CoordinateTuple point(long x) {
		return new DefaultCoordinateTuple(singletonList(xAxis().create(x)));
	}

	public static CoordinateTuple point(long x, long y) {
		return new DefaultCoordinateTuple(asList(xAxis().create(x), yAxis().create(y)));
	}

	public static CoordinateTuple point(long x, long y, long z) {
		return new DefaultCoordinateTuple(asList(xAxis().create(x), yAxis().create(y), zAxis().create(z)));
	}

}
