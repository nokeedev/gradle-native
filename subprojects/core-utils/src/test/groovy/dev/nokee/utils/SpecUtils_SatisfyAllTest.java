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

import static dev.nokee.utils.SpecUtils.satisfyAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecUtils_SatisfyAllTest {
	@Test
	void checkToString() {
		assertThat(satisfyAll(), hasToString("SpecUtils.satisfyAll()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(satisfyAll(), satisfyAll())
			.addEqualityGroup((Spec<Object>) t -> false)
			.testEquals();
	}

	@Test
	void alwaysReturnTrue() {
		assertAll(() -> {
			assertTrue(satisfyAll().isSatisfiedBy("foo"));
			assertTrue(satisfyAll().isSatisfiedBy(42));
			assertTrue(satisfyAll().isSatisfiedBy(new Object()));
		});
	}
}
