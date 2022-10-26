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

import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.CodeablePBXSourcesBuildPhase;
import dev.nokee.xcode.project.KeyedObject;
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
class BuildPhaseFactoryTests {
	@Mock KeyedObject map;
	@InjectMocks BuildPhaseFactory<?> subject;

	@Nested
	class WhenIsaPBXCopyFilesBuildPhase {
		@BeforeEach
		void givenIsaPBXCopyFilesBuildPhase() {
			when(map.isa()).thenReturn("PBXCopyFilesBuildPhase");
		}

		@Test
		void createsPBXCopyFilesBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXCopyFilesBuildPhase(map)));
		}
	}

	@Nested
	class WhenIsaPBXFrameworksBuildPhase {
		@BeforeEach
		void givenIsaPBXFrameworksBuildPhase() {
			when(map.isa()).thenReturn("PBXFrameworksBuildPhase");
		}

		@Test
		void createsPBXFrameworksBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXFrameworksBuildPhase(map)));
		}
	}

	@Nested
	class WhenIsaPBXHeadersBuildPhase {
		@BeforeEach
		void givenIsaPBXHeadersBuildPhase() {
			when(map.isa()).thenReturn("PBXHeadersBuildPhase");
		}

		@Test
		void createsPBXHeadersBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXHeadersBuildPhase(map)));
		}
	}

	@Nested
	class WhenIsaPBXResourcesBuildPhase {
		@BeforeEach
		void givenIsaPBXResourcesBuildPhase() {
			when(map.isa()).thenReturn("PBXResourcesBuildPhase");
		}

		@Test
		void createsPBXResourcesBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXResourcesBuildPhase(map)));
		}
	}

	@Nested
	class WhenIsaPBXShellScriptBuildPhase {
		@BeforeEach
		void givenIsaPBXShellScriptBuildPhase() {
			when(map.isa()).thenReturn("PBXShellScriptBuildPhase");
		}

		@Test
		void createsPBXShellScriptBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXShellScriptBuildPhase(map)));
		}
	}

	@Nested
	class WhenIsaPBXSourcesBuildPhase {
		@BeforeEach
		void givenIsaPBXSourcesBuildPhase() {
			when(map.isa()).thenReturn("PBXSourcesBuildPhase");
		}

		@Test
		void createsPBXSourcesBuildPhase() {
			assertThat(subject.create(map), equalTo(new CodeablePBXSourcesBuildPhase(map)));
		}
	}
}
