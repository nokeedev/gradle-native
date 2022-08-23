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
package dev.nokee.xcode.objects.swiftpackage;

import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.builder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class XCRemoteSwiftPackageReferenceBuilderTest {
	@Test
	void throwsExceptionIfRepositoryUrlIsNull() {
		assertThrows(RuntimeException.class, () -> builder().requirement(mock(XCRemoteSwiftPackageReference.VersionRequirement.class)).build());
	}

	@Test
	void throwsExceptionIfRequirementIsNull() {
		assertThrows(RuntimeException.class, () -> builder().repositoryUrl("https://github.com/examplecom/example").build());
	}

	@Test
	void createsSwiftPackageReference() {
		assertDoesNotThrow(() -> builder().requirement(mock(XCRemoteSwiftPackageReference.VersionRequirement.class)).repositoryUrl("https://github.com/examplecom/example").build());
	}
}
