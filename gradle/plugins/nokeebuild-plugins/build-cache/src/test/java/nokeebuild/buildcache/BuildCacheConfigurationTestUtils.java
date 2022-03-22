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

import org.gradle.api.Action;
import org.gradle.caching.configuration.BuildCacheConfiguration;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.caching.local.DirectoryBuildCache;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public final class BuildCacheConfigurationTestUtils {
	@SuppressWarnings("Unchecked")
	public static BuildCacheConfiguration mock() {
		final HttpBuildCache remoteBuildCache = Mockito.spy(HttpBuildCache.class);
		final DirectoryBuildCache localBuildCache = Mockito.spy(DirectoryBuildCache.class);

		final BuildCacheConfiguration result = Mockito.mock(BuildCacheConfiguration.class);
		Mockito.doAnswer(callActionWith(1, remoteBuildCache)).when(result).remote(eq(HttpBuildCache.class), any());
		Mockito.when(result.remote(HttpBuildCache.class)).thenReturn(remoteBuildCache);
		Mockito.doAnswer(callActionWith(0, localBuildCache)).when(result).local(any(Action.class));
		Mockito.when(result.getLocal()).thenReturn(localBuildCache);

		return result;
	}

	private static Answer<Void> callActionWith(int idx, Object value) {
		return new Answer<Void>() {
			@Override
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocation) throws Throwable {
				invocation.getArgument(idx, Action.class).execute(value);
				return null;
			}
		};
	}
}
