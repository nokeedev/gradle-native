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

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface KnownOperatingSystemFamilyTester {
	OperatingSystemFamily createSubject(String name);

	default Stream<OperatingSystemFamilyUnderTest> provideKnownOperatingSystemFamiliesUnderTest() {
		return OperatingSystemFamilyTestUtils.knownOperatingSystemFamilies();
	}

	@ParameterizedTest(name = "can detect known OS family type [{arguments}]")
	@MethodSource("provideKnownOperatingSystemFamiliesUnderTest")
	default void canDetectKnownOperatingSystemFamilyType(OperatingSystemFamilyUnderTest osFamily) {
		osFamily.assertKnownOperatingSystemFamilyDetected(createSubject(osFamily.getName()));
	}
}
