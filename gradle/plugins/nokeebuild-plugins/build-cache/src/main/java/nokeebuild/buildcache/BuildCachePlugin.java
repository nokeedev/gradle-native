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

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Optional;

class BuildCachePlugin implements Plugin<Settings> {
	private final ProviderFactory providers;

	@Inject
	public BuildCachePlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Settings settings) {
		settings.buildCache(new UseRemoteBuildCache(new RemoteBuildCacheParameters()));
		settings.buildCache(new UseLocalBuildCache(new LocalBuildCacheParameters()));
	}

	@SuppressWarnings("UnstableApiUsage")
	private final class RemoteBuildCacheParameters implements UseRemoteBuildCache.RemoteBuildCacheParameters {
		private static final String GRADLE_CACHE_REMOTE_URL_PROPERTY_NAME = "gradle.cache.remote.url";
		private static final String GRADLE_CACHE_REMOTE_URL_ENV_NAME = "GRADLE_CACHE_REMOTE_URL";
		private static final String GRADLE_CACHE_REMOTE_USERNAME_PROPERTY_NAME = "gradle.cache.remote.username";
		private static final String GRADLE_CACHE_REMOTE_USERNAME_ENV_NAME = "GRADLE_CACHE_REMOTE_USERNAME";
		private static final String GRADLE_CACHE_REMOTE_PASSWORD_PROPERTY_NAME = "gradle.cache.remote.password";
		private static final String GRADLE_CACHE_REMOTE_PASSWORD_ENV_NAME = "GRADLE_CACHE_REMOTE_PASSWORD";
		private static final String GRADLE_CACHE_REMOTE_PUSH_PROPERTY_NAME = "gradle.cache.remote.push";

		@Override
		public Optional<String> remoteBuildCacheUrl() {
			return Optional.ofNullable(providers.environmentVariable(GRADLE_CACHE_REMOTE_URL_ENV_NAME).forUseAtConfigurationTime()
				.orElse(providers.systemProperty(GRADLE_CACHE_REMOTE_URL_PROPERTY_NAME).forUseAtConfigurationTime())
				.orElse("https://ge.nokee.dev/cache/")
				.getOrNull());
		}

		@Override
		public boolean allowPushToRemote() {
			return providers.systemProperty(GRADLE_CACHE_REMOTE_PUSH_PROPERTY_NAME).forUseAtConfigurationTime().map(Boolean::parseBoolean).orElse(false).get();
		}

		@Override
		public String remoteBuildCacheUsername() {
			return providers.environmentVariable(GRADLE_CACHE_REMOTE_USERNAME_ENV_NAME).forUseAtConfigurationTime()
				.orElse(providers.systemProperty(GRADLE_CACHE_REMOTE_USERNAME_PROPERTY_NAME).forUseAtConfigurationTime())
				.getOrNull();
		}

		@Override
		public String remoteBuildCachePassword() {
			return providers.environmentVariable(GRADLE_CACHE_REMOTE_PASSWORD_ENV_NAME).forUseAtConfigurationTime()
				.orElse(providers.systemProperty(GRADLE_CACHE_REMOTE_PASSWORD_PROPERTY_NAME).forUseAtConfigurationTime())
				.getOrNull();
		}
	}

	private final class LocalBuildCacheParameters implements UseLocalBuildCache.LocalBuildCacheParameters {
		@Override
		public boolean localBuildCacheDisabled() {
			return false;
		}
	}
}
