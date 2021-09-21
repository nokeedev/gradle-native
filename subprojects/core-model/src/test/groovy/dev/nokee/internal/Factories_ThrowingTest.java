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
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.alwaysThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(Factories.class)
class Factories_ThrowingTest {
	@Test
	void alwaysThrowException() {
		assertThrows(UnsupportedOperationException.class, () -> alwaysThrow().create());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(alwaysThrow(), alwaysThrow())
			.addEqualityGroup((Factory<?>)() -> null)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(alwaysThrow(), hasToString("Factories.alwaysThrow()"));
	}
}
