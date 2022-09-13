/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.core.exec;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.core.exec.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;

interface CommandLineToolInvocationOutputRedirectionTester<T> {
	T subject();

	@Test
	default void canSerialize() {
		assertThat(subject(), isSerializable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		final EqualsTester tester = new EqualsTester();
		testEquals(tester);
		tester.testEquals();
	}

	@SuppressWarnings("UnstableApiUsage")
	void testEquals(EqualsTester tester);

	@Test
	default void checkToString() {
		testToString(subject().toString());
	}

	void testToString(String subjectToString);
}
