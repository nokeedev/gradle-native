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
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateAxis.of;

class CoordinateAxisEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(TestAxis.class), of(TestAxis.class), of(TestAxis.class, "test-axis"))
			.addEqualityGroup(of(TestAxis.class, "foo"))
			.addEqualityGroup(of(TestAxis.class, "bar"))
			.addEqualityGroup(of(Object.class))
			.addEqualityGroup(of(Object.class, "foo"))
			.testEquals();
	}
}
