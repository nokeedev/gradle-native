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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.CodeablePBXFileReference;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileReferenceFactoryTests {
	@Mock KeyedObject map;
	@InjectMocks FileReferenceFactory<?> subject;

	@Nested
	class WhenIsaPBXFileReference {
		@BeforeEach
		void givenIsaPBXFileReference() {
			when(map.isa()).thenReturn("PBXFileReference");
		}

		@Test
		void createsPBXFileReference() {
			assertThat(subject.create(map), equalTo(new CodeablePBXFileReference(map)));
		}
	}

	@Nested
	class WhenIsaPBXReferenceProxy {
		@BeforeEach
		void givenIsaPBXReferenceProxy() {
			when(map.isa()).thenReturn("PBXReferenceProxy");
		}

		@Test
		void createsPBXReferenceProxy() {
			assertThat(subject.create(map), equalTo(new CodeablePBXReferenceProxy(map)));
		}
	}

	@Nested
	class WhenIsaPBXVariantGroup {
		@BeforeEach
		void givenIsaPBXVariantGroup() {
			when(map.isa()).thenReturn("PBXVariantGroup");
		}

		@Test
		void createsPBXVariantGroup() {
			assertThat(subject.create(map), equalTo(new CodeablePBXVariantGroup(map)));
		}
	}

	@Nested
	class WhenIsaXCVersionGroup {
		@BeforeEach
		void givenIsaXCVersionGroup() {
			when(map.isa()).thenReturn("XCVersionGroup");
		}

		@Test
		void createsXCVersionGroup() {
			assertThat(subject.create(map), equalTo(new CodeableXCVersionGroup(map)));
		}
	}
}
