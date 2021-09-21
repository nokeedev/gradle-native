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

import dev.nokee.internal.testing.Assumptions;

import java.util.Arrays;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class DefaultCoordinateSetTest implements CoordinateSetFactoryTester {
	@Override
	public <T extends Enum<T>> CoordinateSet<T> createSubject(Class<T> type) {
		return Assumptions.skipCurrentTestExecution("Testing new DefaultCoordinateSet(Set<Coordinate<T>>)");
	}

	@Override
	public <T> CoordinateSet<T> createSubject(Coordinate<T>... coordinates) {
		return new DefaultCoordinateSet<>(new HashSet<>(asList(coordinates)));
	}

	@Override
	public <T> CoordinateSet<T> createSubject(CoordinateAxis<T> axis, T... values) {
		return new DefaultCoordinateSet<>(Arrays.stream(values).map(axis::create).collect(toSet()));
	}
}
