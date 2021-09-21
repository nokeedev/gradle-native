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

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

interface UnknownOperatingSystemFamilyTester {
	OperatingSystemFamily createSubject(String name);

	@Test
	default void unknownFamilyNameIsNotAnyOfKnownFamilyType() {
		val subject = createSubject("unknown-os");
		assertAll(
			() -> assertFalse(subject.isWindows(), "windows family"),
			() -> assertFalse(subject.isFreeBSD(), "freeBSD family"),
			() -> assertFalse(subject.isLinux(), "linux family"),
			() -> assertFalse(subject.isMacOs(), "macOS family"),
			() -> assertFalse(subject.isIos(), "iOS family"),
			() -> assertFalse(subject.isHewlettPackardUnix(), "HP-UX family"),
			() -> assertFalse(subject.isSolaris(), "solaris family")
		);
	}
}
