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

import java.util.Optional;

import static nokeebuild.buildcache.HttpBuildCacheUtils.credentials;
import static nokeebuild.buildcache.HttpBuildCacheUtils.enabled;
import static nokeebuild.buildcache.HttpBuildCacheUtils.push;
import static nokeebuild.buildcache.HttpBuildCacheUtils.url;
import static nokeebuild.buildcache.PropertyUtils.set;

final class UseRemoteBuildCache implements Action<BuildCacheConfiguration> {
    private final RemoteBuildCacheParameters remoteParameters;

    UseRemoteBuildCache(RemoteBuildCacheParameters remoteParameters) {
        this.remoteParameters = remoteParameters;
    }

    @Override
    public void execute(BuildCacheConfiguration buildCache) {
        remoteParameters.remoteBuildCacheUrl().ifPresent(it -> buildCache.remote(HttpBuildCache.class, url(set(it))));
        buildCache.remote(HttpBuildCache.class, credentials(new ForRemoteBuildCache(remoteParameters)));
        buildCache.remote(HttpBuildCache.class, enabled(new OnlyIfCredentialsAvailable()));
        buildCache.remote(HttpBuildCache.class, push(set(remoteParameters.allowPushToRemote())));
    }

    interface RemoteBuildCacheParameters extends ForRemoteBuildCache.RemoteBuildCacheCredentials {
        Optional<String> remoteBuildCacheUrl();
        boolean allowPushToRemote();
    }
}
