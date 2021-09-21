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

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

interface UnknownMachineArchitectureTester {
	MachineArchitecture createSubject(String name);

	@Test
	default void throwsExceptionWhenChecking32BitPointerSizeDueToUnknownArchitectureNae() {
		assertThrows(UnsupportedOperationException.class, () -> createSubject("unknown-arch").is32Bit());
	}

	@Test
	default void throwsExceptionWhenChecking64BitPointerSizeDueToUnknownArchitectureNae() {
		assertThrows(UnsupportedOperationException.class, () -> createSubject("unknown-arch").is64Bit());
	}

	@Test
	default void usesArchitectureNameAsIsButLoweredCaseForCanonicalNameWhenUnknown() {
		assertAll(
			() -> assertThat(createSubject("unknown-arch").getCanonicalName(), equalTo("unknown-arch")),
			() -> assertThat(createSubject("Unknown-ARCH").getCanonicalName(), equalTo("unknown-arch"))
		);
	}
}
