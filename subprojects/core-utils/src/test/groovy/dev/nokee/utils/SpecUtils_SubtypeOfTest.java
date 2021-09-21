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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.subtypeOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SpecUtils_SubtypeOfTest {
	@Test
	void returnsTrueWhenClassIsSubtypeOfSpecifiedClass() {
		assertThat(subtypeOf(Number.class).isSatisfiedBy(Long.class), equalTo(true));
	}

	@Test
	void returnsFalseWhenClassIsNotSubtypeOfSpecifiedClass() {
		assertThat(subtypeOf(String.class).isSatisfiedBy(Long.class), equalTo(false));
	}

	@Test
	void returnsEnhanceSpec() {
		assertThat(subtypeOf(String.class), isA(SpecUtils.Spec.class));
	}

	@Test
	void checkToString() {
		assertThat(subtypeOf(String.class), hasToString("SpecUtils.subtypeOf(class java.lang.String)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(subtypeOf(String.class), subtypeOf(String.class))
			.addEqualityGroup(subtypeOf(Number.class))
			.addEqualityGroup(subtypeOf(Object.class), satisfyAll())
			.testEquals();
	}
}
