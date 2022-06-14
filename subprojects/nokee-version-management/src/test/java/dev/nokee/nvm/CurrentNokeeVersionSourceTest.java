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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.nvm.NokeeVersion.version;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurrentNokeeVersionSourceTest {
	CurrentNokeeVersionSource.Parameters parameters = objectFactory().newInstance(CurrentNokeeVersionSource.Parameters.class);
	NokeeVersionLoader loader = Mockito.mock(NokeeVersionLoader.class);
	CurrentNokeeVersionSource source = new CurrentNokeeVersionSource(loader) {
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
	void doesNotTryToLoadVersionFromUrlWhenLoadSuccessfulFromFile() {
		when(loader.fromFile(any())).thenReturn(version("2.1.4"));
		source.obtain();
		verify(loader, never()).fromUrl(any());
	}

	@Test
	void doesNotTryToLoadVersionFromUrlWhenFailToLoadFromFileAndNetworkAccessDisallowed() {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.DISALLOWED);
		when(loader.fromFile(any())).thenReturn(null);
		source.obtain();
		verify(loader, never()).fromUrl(any());
	}

	@Test
	void returnsNoValueWhenFailToLoadFromFileAndNetworkAccessDisallowed() {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.DISALLOWED);
		when(loader.fromFile(any())).thenReturn(null);
		assertThat(source.obtain(), nullValue());
	}

	@Test
	void loadsVersionFromNokeeServiceUrlByDefaultWhenFailToLoadFromFileAndNetworkAccessAllowed() throws MalformedURLException {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED);
		when(loader.fromFile(any())).thenReturn(null);
		source.obtain();
		verify(loader).fromUrl(new URL("https://services.nokee.dev/versions/current.json"));
	}

	@Test
	void loadsVersionFromUrlInParametersWhenFailToLoadFromFileAndNetworkAccessAllowed() throws MalformedURLException, URISyntaxException {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED);
		parameters.getCurrentReleaseUrl().set(new URI("https://example.com/versions/my-current.json"));
		when(loader.fromFile(any())).thenReturn(null);
		source.obtain();
		verify(loader).fromUrl(new URL("https://example.com/versions/my-current.json"));
	}

	@Test
	void returnsNoValueWhenFailToLoadFromFileAndUrlAndNetworkAccessAllowed() {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED);
		when(loader.fromFile(any())).thenReturn(null);
		when(loader.fromUrl(any())).thenReturn(null);
		assertThat(source.obtain(), nullValue());
	}

	@Test
	void returnsLoadedVersionFromUrlWhenFailToLoadFromFileAndNetworkAccessAllowed() throws MalformedURLException, URISyntaxException {
		parameters.getNetworkStatus().set(CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED);
		when(loader.fromFile(any())).thenReturn(null);
		when(loader.fromUrl(any())).thenReturn(version("4.5.6"));
		assertThat(source.obtain(), equalTo(version("4.5.6")));
	}
}
