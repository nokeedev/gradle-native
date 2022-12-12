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
package dev.nokee.util.internal;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlwaysTruePredicateTests {
	AlwaysTruePredicate subject = new AlwaysTruePredicate();

	@Test
	void alwaysReturnTrueRegardlessOfInputValue() {
		assertAll(
			() -> assertTrue(subject.test(1)),
			() -> assertTrue(subject.test(0)),
			() -> assertTrue(subject.test(null))
		);
	}

	@Test
	void canSerialize() {
		assertThat(subject, isSerializable());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester().addEqualityGroup(new AlwaysTruePredicate(), new AlwaysTruePredicate()).testEquals();
	}
}
