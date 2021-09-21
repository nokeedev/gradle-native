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

import static dev.nokee.utils.SpecTestUtils.aSpec;
import static dev.nokee.utils.SpecTestUtils.anotherSpec;
import static dev.nokee.utils.SpecUtils.*;
import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

public interface SpecNegateTester {
	Object IN = new Object();

	<T> SpecUtils.Spec<T> createNegateSpec(SpecUtils.Spec<T> spec);

	@Test
	default void returnsSourceSpecWhenNegatingTwice() {
		assertThat(createNegateSpec(createNegateSpec(aSpec())), equalTo(aSpec()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createNegateSpec(aSpec()), createNegateSpec(aSpec()))
			.addEqualityGroup(createNegateSpec(anotherSpec()))
			.testEquals();
	}

	@Test
	default void canNegateSatisfyingSpec() {
		assertThat(createNegateSpec(it -> true).isSatisfiedBy(IN), equalTo(false));
	}

	@Test
	default void canNegateUnsatisfyingSpec() {
		assertThat(createNegateSpec(it -> false).isSatisfiedBy(IN), equalTo(true));
	}

	@Test
	default void checkToString() {
		assertThat(createNegateSpec(aSpec()), hasToString("SpecUtils.negate(aSpec())"));
	}

	@Test
	default void returnsSatisfyAllWhenNegatingSatisfyNone() {
		assertThat(createNegateSpec(satisfyNone()), equalTo(satisfyAll()));
	}

	@Test
	default void returnsSatisfyNoneWhenNegatingSatisfyAll() {
		assertThat(createNegateSpec(satisfyAll()), equalTo(satisfyNone()));
	}
}
