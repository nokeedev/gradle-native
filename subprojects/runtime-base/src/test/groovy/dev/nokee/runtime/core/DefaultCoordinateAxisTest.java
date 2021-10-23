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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

class DefaultCoordinateAxisTest implements CoordinateAxisTester<TestAxis>, CoordinateAxisFactoryTester {
	@Override
	public CoordinateAxis<TestAxis> createSubject() {
		return new DefaultCoordinateAxis<>(TestAxis.class, "test");
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type) {
		return new DefaultCoordinateAxis<>(type);
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type, String name) {
		return new DefaultCoordinateAxis<>(type, name);
	}

	@Test
	void hasDisplayNameInferredFromAxisType() {
		assertThat(new DefaultCoordinateAxis<>(TestAxis.class).getDisplayName(), is("test axis"));
	}

	@Test
	void checkToString() {
		assertAll(
			() -> assertThat(new DefaultCoordinateAxis<>(TestAxis.class),
				hasToString("axis <test-axis>")),
			() -> assertThat(new DefaultCoordinateAxis<>(TestAxis.class, "custom-name"),
				hasToString("axis <custom-name>"))
		);
	}
}
