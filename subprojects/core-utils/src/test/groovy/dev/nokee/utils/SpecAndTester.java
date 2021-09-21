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
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.SpecTestUtils.*;
import static dev.nokee.utils.SpecUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

public interface SpecAndTester {
	Object IN = new Object();

	<T> Spec<T> createAndSpec(SpecUtils.Spec<T> first, Spec<? super T> second);

	@Test
	default void checkToString() {
		assertThat(createAndSpec(aSpec(), anotherSpec()), hasToString("SpecUtils.and(aSpec(), anotherSpec())"));
	}

	@Test
	default void returnsFirstSpecWhenSecondIsSatisfyingAll() {
		assertThat(createAndSpec(aSpec(), satisfyAll()), equalTo(aSpec()));
	}

	@Test
	default void returnsOneOfTheSpecWhenBothAreTheSameSpec() {
		assertThat(createAndSpec(aSpec(), aSpec()), equalTo(aSpec()));
	}

	@Test
	default void returnsSatisfyingNoneWhenSecondSpecIsSatisfyNone() {
		assertThat(createAndSpec(aSpec(), satisfyNone()), equalTo(satisfyNone()));
	}

	@Test
	default void returnsFalseWhenOneOfTheSpecReturnsFalse() {
		assertThat(createAndSpec(it -> true, it -> false).isSatisfiedBy(IN), equalTo(false));
		assertThat(createAndSpec(it -> false, it -> true).isSatisfiedBy(IN), equalTo(false));
	}

	@Test
	default void returnsTrueWhenBothSpecReturnsTrue() {
		assertThat(createAndSpec(it -> true, it -> true).isSatisfiedBy(IN), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothSpecReturnsFalse() {
		assertThat(createAndSpec(it -> false, it -> false).isSatisfiedBy(IN), equalTo(false));
	}

	@Test
	default void returnsSecondSpecWhenFirstSpecIsSatisfyingAll() {
		assertThat(createAndSpec(satisfyAll(), aSpec()), equalTo(aSpec()));
	}

	@Test
	default void returnsSatisfyingNoneWhenFirstSpecIsSatisfyNone() {
		assertThat(createAndSpec(satisfyNone(), aSpec()), equalTo(satisfyNone()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				createAndSpec(aSpec(), anotherSpec()),
				createAndSpec(aSpec(), anotherSpec()),
				createAndSpec(anotherSpec(), aSpec()))
			.addEqualityGroup(createAndSpec(anotherSpec("a"), anotherSpec("b")))
			.testEquals();
	}
}
