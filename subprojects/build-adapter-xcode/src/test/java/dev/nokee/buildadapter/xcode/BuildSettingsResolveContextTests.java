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

import dev.nokee.buildadapter.xcode.internal.plugins.BuildSettingsResolveContext;
import dev.nokee.xcode.XCBuildSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(MockitoExtension.class)
class BuildSettingsResolveContextTests {
	@Mock XCBuildSettings buildSettings;
	@InjectMocks BuildSettingsResolveContext subject;

	@Nested
	class WhenGetBuiltProductDirectoryCalled {
		@BeforeEach
		void givenBuiltProductDir() {
			Mockito.when(buildSettings.get("BUILT_PRODUCT_DIR")).thenReturn("/derived-data/Products");
		}

		@Test
		void returnsPathToBuiltProductDir() {
			assertThat(subject.getBuiltProductDirectory(), aFile(withAbsolutePath(endsWith("/derived-data/Products"))));
		}
	}

	@Nested
	class WhenGetCalled {
		@BeforeEach
		void givenSourceRoot() {
			Mockito.when(buildSettings.get("SOURCE_ROOT")).thenReturn("/test/Foo-Project");
		}

		@Test
		void returnsPathToSourceRoot() {
			assertThat(subject.get("SOURCE_ROOT"), aFile(withAbsolutePath(endsWith("/test/Foo-Project"))));
		}
	}
}
