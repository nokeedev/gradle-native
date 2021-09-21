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
package dev.nokee.runtime.nativebase;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.nativebase.OperatingSystemFamilyTestUtils.knownOperatingSystemFamilies;
import static org.hamcrest.MatcherAssert.assertThat;

public interface OperatingSystemFamilyTester extends NamedValueTester<OperatingSystemFamily>, KnownOperatingSystemFamilyTester, UnknownOperatingSystemFamilyTester {
	default Stream<OperatingSystemFamilyUnderTest> provideOperatingSystemFamiliesUnderTest() {
		return knownOperatingSystemFamilies();
	}

	@Override
	default Stream<String> knownValues() {
		return knownOperatingSystemFamilies().map(OperatingSystemFamilyUnderTest::getName);
	}

	@ParameterizedTest(name = "has name [{arguments}]")
	@MethodSource("provideOperatingSystemFamiliesUnderTest")
	default void hasName(OperatingSystemFamilyUnderTest osFamily) {
		assertThat(createSubject(osFamily.getName()), named(osFamily.getName()));
	}

	@ParameterizedTest(name = "has canonical name [{arguments}]")
	@MethodSource("provideOperatingSystemFamiliesUnderTest")
	default void hasCanonicalName(OperatingSystemFamilyUnderTest osFamily) {
		osFamily.assertCanonicalName(createSubject(osFamily.getName()));
	}
}
