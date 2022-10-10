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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.WarnOnMissingXCProjectsTransformer;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarnOnMissingXCProjectsTransformerTests {
	@Mock Consumer<String> logger;
	@Mock Path basePath;
	@InjectMocks WarnOnMissingXCProjectsTransformer subject;

	@Nested
	class WhenNoProjects {
		Iterable<XCProjectReference> result;

		@BeforeEach
		void givenNoProjectTransformation() {
			when(basePath.toString()).thenReturn("/test");
			result = subject.transform(of());
		}

		@Test
		void logsWarningMessage() {
			verify(logger).accept("The plugin 'dev.nokee.xcode-build-adapter' has no effect because no Xcode workspace or project were found in '/test'. See https://nokee.fyi/using-xcode-build-adapter for more details.");
		}

		@Test
		void returnsInputProjects() {
			assertThat(result, emptyIterable());
		}
	}

	@Nested
	class WhenProjects {
		Iterable<XCProjectReference> result;

		@BeforeEach
		void givenNoProjectTransformation() {
			result = subject.transform(of(project("A"), project("B")));
		}

		@Test
		void doesNotLogAnyMessage() {
			verify(logger, never()).accept(any());
		}

		@Test
		void returnsInputProjects() {
			assertThat(result, containsInAnyOrder(project("A"), project("B")));
		}
	}
}
