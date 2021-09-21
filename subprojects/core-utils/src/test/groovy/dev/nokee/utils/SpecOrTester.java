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
import static dev.nokee.utils.SpecUtils.or;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

public interface SpecOrTester {
	Object IN = new Object();

	<T> Spec<T> createOrSpec(SpecUtils.Spec<T> first, Spec<? super T> second);

	@Test
	default void returnsFirstSpecWhenSecondSpecIsSatisfyingNone() {
		assertThat(createOrSpec(aSpec(), satisfyNone()), equalTo(aSpec()));
	}

	@Test
	default void returnsSatisfyingAllWhenSecondSpecIsSatisfyAll() {
		assertThat(createOrSpec(aSpec(), satisfyAll()), equalTo(satisfyAll()));
	}

	@Test
	default void returnsOneOfTheSpecWhenBothAreTheSameSpec() {
		assertThat(createOrSpec(aSpec(), aSpec()), equalTo(aSpec()));
	}

	@Test
	default void returnsSecondSpecWhenFirstIsSatisfyingNone() {
		assertThat(createOrSpec(satisfyNone(), aSpec()), equalTo(aSpec()));
	}

	@Test
	default void returnsTrueWhenOneOfTheSpecReturnsTrue() {
		assertThat(createOrSpec(t -> true, t -> false).isSatisfiedBy(IN), equalTo(true));
		assertThat(createOrSpec(t -> false, t -> true).isSatisfiedBy(IN), equalTo(true));
	}

	@Test
	default void returnsTrueWhenBothSpecReturnsTrue() {
		assertThat(createOrSpec(t -> true, t -> true).isSatisfiedBy(IN), equalTo(true));
	}

	@Test
	default void returnsFalseWhenBothSpecReturnsFalse() {
		assertThat(createOrSpec(t-> false, t -> false).isSatisfiedBy(IN), equalTo(false));
	}

	@Test
	default void checkToString() {
		assertThat(createOrSpec(aSpec(), anotherSpec()),
			hasToString("SpecUtils.or(aSpec(), anotherSpec())"));
	}

	@Test
	default void returnsSatisfyingAllWhenFirstSpecIsSatisfyAll() {
		assertThat(createOrSpec(satisfyAll(), aSpec()), equalTo(satisfyAll()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				createOrSpec(aSpec(), anotherSpec()),
				createOrSpec(aSpec(), anotherSpec()),
				createOrSpec(anotherSpec(), aSpec()))
			.addEqualityGroup(createOrSpec(anotherSpec("a"), anotherSpec("b")))
			.testEquals();
	}
}
