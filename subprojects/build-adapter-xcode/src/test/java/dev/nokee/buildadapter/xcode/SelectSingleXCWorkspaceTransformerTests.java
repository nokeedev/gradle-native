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

import com.google.common.collect.ImmutableSet;
import dev.nokee.buildadapter.xcode.internal.plugins.SelectSingleXCWorkspaceTransformer;
import dev.nokee.xcode.XCWorkspaceReference;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.buildadapter.xcode.TestWorkspaceReference.workspace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SelectSingleXCWorkspaceTransformerTests {
	@Mock Consumer<String> logger;
	@InjectMocks SelectSingleXCWorkspaceTransformer subject;
	XCWorkspaceReference result;

	@Nested
	class WhenNoWorkspaceTransformed {
		@BeforeEach
		void givenNoWorkspaceTransformation() {
			result = subject.transform(of());
		}

		@Test
		void returnsNull() {
			assertThat(result, nullValue());
		}

		@Test
		void doesNotLogAnyMessage() {
			verify(logger, never()).accept(any());
		}
	}

	@Nested
	class WhenSingleWorkspaceTransformed {
		@BeforeEach
		void givenSingleWorkspaceTransformation() {
			result = subject.transform(of(workspace("A")));
		}

		@Test
		void returnsWorkspace() {
			assertThat(result, equalTo(workspace("A")));
		}

		@Test
		void doesNotLogAnyMessage() {
			verify(logger, never()).accept(any());
		}
	}

	@Nested
	class WhenMultipleWorkspaceTransformed {
		@BeforeEach
		void givenMultipleWorkspaceTransformation() {
			result = subject.transform(of(workspace("A"), workspace("B")));
		}

		@Test
		void returnsFirstWorkspace() {
			assertThat(result, equalTo(workspace("A")));
		}

		@Test
		void logsWarningMessage() {
			verify(logger).accept("The plugin 'dev.nokee.xcode-build-adapter' will use Xcode workspace located at '" + Paths.get("/test") + "/ A.xcworkspace' because multiple Xcode workspace were found in '" + Paths.get("/test") + "'. See https://nokee.fyi/using-xcode-build-adapter for more details.");
		}
	}
}
