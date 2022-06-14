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
package dev.nokee.nvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.nvm.NokeeVersion.version;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NokeeVersionSourceTest {
	NokeeVersionSource.Parameters parameters = objectFactory().newInstance(NokeeVersionSource.Parameters.class);
	NokeeVersionLoader loader = Mockito.mock(NokeeVersionLoader.class);
	NokeeVersionSource source = new NokeeVersionSource(loader) {
		@Override
		public Parameters getParameters() {
			return parameters;
		}
	};

	@BeforeEach
	void setUp() {
		parameters.getNokeeVersionFile().set(new File("/a/b/c/.nokee-version"));
	}

	@Test
	void loadsVersionFromFileInParameters() {
		source.obtain();
		verify(loader).fromFile(new File("/a/b/c/.nokee-version").toPath());
	}

	@Test
	void returnsLoadedVersionFromFile() {
		when(loader.fromFile(any())).thenReturn(version("1.3.4"));
		assertThat(source.obtain(), equalTo(version("1.3.4")));
	}

	@Test
	void returnsNullWhenNoVersionsAreLoaded() {
		when(loader.fromFile(any())).thenReturn(null);
		assertThat(source.obtain(), nullValue());
	}
}
