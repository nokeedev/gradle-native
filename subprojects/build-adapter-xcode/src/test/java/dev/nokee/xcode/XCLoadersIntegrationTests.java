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
package dev.nokee.xcode;

import dev.nokee.xcode.objects.PBXProject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static org.hamcrest.MatcherAssert.assertThat;

class XCLoadersIntegrationTests {
	@Nested
	class WhenWorkspaceProjectReferenceLoader {
		XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> subject = XCLoaders.workspaceProjectReferencesLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Nested
	class WhenCrossProjectReferencesLoader {
		XCLoader<Iterable<XCProjectReference>, XCProjectReference> subject = XCLoaders.crossProjectReferencesLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Nested
	class WhenTargetConfigurationsLoader {
		XCLoader<Set<String>, XCTargetReference> subject = XCLoaders.targetConfigurationsLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Nested
	class WhenDefaultTargetConfigurationLoader {
		XCLoader<String, XCTargetReference> subject = XCLoaders.defaultTargetConfigurationLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Nested
	class WhenAllTargetsLoader {
		XCLoader<Set<XCTargetReference>, XCProjectReference> subject = XCLoaders.allTargetsLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}

	@Nested
	class WhenPBXProjectLoader {
		XCLoader<PBXProject, XCProjectReference> subject = XCLoaders.pbxprojectLoader();

		@Test
		void canSerialize() {
			assertThat(subject, isSerializable());
		}
	}
}
