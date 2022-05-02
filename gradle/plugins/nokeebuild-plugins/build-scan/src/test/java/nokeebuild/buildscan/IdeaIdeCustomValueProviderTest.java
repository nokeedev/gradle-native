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
package nokeebuild.buildscan;

import com.gradle.scan.plugin.BuildScanExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static nokeebuild.buildscan.IdeaIdeCustomValueProvider.IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES;
import static nokeebuild.buildscan.IdeaIdeCustomValueProvider.IDEA_VERSION_SYSTEM_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaIdeCustomValueProviderTest {
	@Mock private IdeaIdeCustomValueProvider.Parameters ideaRuntime;
	@InjectMocks private IdeaIdeCustomValueProvider subject;
	@Mock private BuildScanExtension buildScan;

	@Test
	void hasIdeaVersionSystemPropertyName() {
		assertEquals("idea.paths.selector", IDEA_VERSION_SYSTEM_PROPERTY_NAME);
	}

	@Test
	void hasIdeaRuntimeSystemPropertyNames() {
		assertTrue(Arrays.asList(IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES).contains("idea.registered"));
		assertTrue(Arrays.asList(IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES).contains("idea.active"));
		assertTrue(Arrays.asList(IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES).contains("idea.paths.selector"));
	}

	@Nested
	class WhenNoLaunchedFromIdeaTest {
		@BeforeEach
		void setUp() {
			when(ideaRuntime.wasLaunchedFromIdea()).thenReturn(false);
		}

		@Test
		void doesNotIncludeAnyTagsOrValuesWhenNotLaunchedFromIdea() {
			subject.execute(buildScan);
			verify(buildScan, never()).tag(any());
			verify(buildScan, never()).value(any(), any());
		}
	}

	@Nested
	class WhenLaunchedFromIdeaTest {
		@BeforeEach
		void setUp() {
			when(ideaRuntime.wasLaunchedFromIdea()).thenReturn(true);
			when(ideaRuntime.ideaVersion()).thenReturn(Optional.empty());
		}

		@Test
		void tagsBuildScanWithIdeaTagWhenLaunchedFromIdea() {
			subject.execute(buildScan);
			verify(buildScan).tag("IDEA");
		}

		@Test
		void doesNotAddIdeaVersionWhenIdeaVersionNotAvailable() {
			subject.execute(buildScan);
			verify(buildScan, never()).value(eq("ideaVersion"), any());
		}

		@Test
		void addsIdeaVersionWhenIdeaVersionIsAvailable() {
			when(ideaRuntime.ideaVersion()).thenReturn(Optional.of("2021.3"));
			subject.execute(buildScan);
			verify(buildScan).value(eq("ideaVersion"), eq("2021.3"));
		}
	}
}
