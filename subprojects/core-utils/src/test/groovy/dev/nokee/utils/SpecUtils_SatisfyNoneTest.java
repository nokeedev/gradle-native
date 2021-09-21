/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;

class SpecUtils_SatisfyNoneTest {
	@Test
	void checkToString() {
		assertThat(satisfyNone(), hasToString("SpecUtils.satisfyNone()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(satisfyNone(), satisfyNone())
			.addEqualityGroup((Spec<Object>) t -> true)
			.testEquals();
	}

	@Test
	void alwaysReturnFalse() {
		assertAll(() -> {
			assertFalse(satisfyNone().isSatisfiedBy("foo"));
			assertFalse(satisfyNone().isSatisfiedBy(42));
			assertFalse(satisfyNone().isSatisfiedBy(new Object()));
		});
	}
}
