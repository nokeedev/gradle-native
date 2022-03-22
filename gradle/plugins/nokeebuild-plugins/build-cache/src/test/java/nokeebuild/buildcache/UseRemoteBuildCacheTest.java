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
package nokeebuild.buildcache;

import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseRemoteBuildCacheTest {
	@Mock private UseRemoteBuildCache.RemoteBuildCacheParameters parameters;
	@InjectMocks private UseRemoteBuildCache subject;
	private final BuildCacheConfiguration buildCache = BuildCacheConfigurationTestUtils.mock();

	@BeforeEach
	void configureParameters() {
		when(parameters.remoteBuildCacheUrl()).thenReturn(Optional.of("https://my-cache.company.com/"));
		when(parameters.remoteBuildCacheUsername()).thenReturn("my-username");
		when(parameters.remoteBuildCachePassword()).thenReturn("my-password");
	}

	@Test
	void configureRemoteBuildCacheUrlUsingParameters() throws URISyntaxException {
		subject.execute(buildCache);
		assertEquals(new URI("https://my-cache.company.com/"), buildCache.remote(HttpBuildCache.class).getUrl());
	}

	@Test
	void doesNotTryToSetRemoveBuildCacheUrlWhenAbsent() {
		when(parameters.remoteBuildCacheUrl()).thenReturn(Optional.empty());

		assertDoesNotThrow(() -> subject.execute(buildCache));
	}

	@Test
	void configureRemoteBuildCacheCredentialsUsingParameters() {
		subject.execute(buildCache);
		assertEquals("my-username", buildCache.remote(HttpBuildCache.class).getCredentials().getUsername());
		assertEquals("my-password", buildCache.remote(HttpBuildCache.class).getCredentials().getPassword());
	}

	@Test
	void enablesRemoteBuildCacheIfCredentialsPresent() {
		subject.execute(buildCache);
		assertTrue(buildCache.remote(HttpBuildCache.class).isEnabled());
	}

	@Test
	void disablesRemoteBuildCacheIfUsernameAbsent() {
		when(parameters.remoteBuildCacheUsername()).thenReturn(null);

		subject.execute(buildCache);
		assertFalse(buildCache.remote(HttpBuildCache.class).isEnabled());
	}

	@Test
	void disablesRemoteBuildCacheIfPasswordAbsent() {
		when(parameters.remoteBuildCachePassword()).thenReturn(null);

		subject.execute(buildCache);
		assertFalse(buildCache.remote(HttpBuildCache.class).isEnabled());
	}

	@Test
	void enablesPushToRemoteBuildCacheWhenAllowed() {
		when(parameters.allowPushToRemote()).thenReturn(true);

		subject.execute(buildCache);
		assertTrue(buildCache.remote(HttpBuildCache.class).isPush());
	}

	@Test
	void disablesPushToRemoteBuildCacheWhenAllowed() {
		when(parameters.allowPushToRemote()).thenReturn(false);

		subject.execute(buildCache);
		assertFalse(buildCache.remote(HttpBuildCache.class).isPush());
	}
}
