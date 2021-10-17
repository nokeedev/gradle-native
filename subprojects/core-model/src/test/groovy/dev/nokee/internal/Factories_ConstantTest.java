/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.internal;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.Factories.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class Factories_ConstantTest {
	@Test
	void alwaysReturnTheSpecifiedConstant() {
		val factory = constant(42);
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
		assertThat(factory.create(), equalTo(42));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(constant(42), constant(42))
			.addEqualityGroup(constant(24))
			.addEqualityGroup(constant("foo"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(constant(42), hasToString("Factories.constant(42)"));
	}
}
