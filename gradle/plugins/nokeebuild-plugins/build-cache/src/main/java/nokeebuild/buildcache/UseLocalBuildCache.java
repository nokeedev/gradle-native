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
import org.gradle.caching.local.DirectoryBuildCache;

import static nokeebuild.buildcache.HttpBuildCacheUtils.enabled;
import static nokeebuild.buildcache.PropertyUtils.set;

final class UseLocalBuildCache implements Action<BuildCacheConfiguration> {
    private final LocalBuildCacheParameters localParameters;

    UseLocalBuildCache(LocalBuildCacheParameters localParameters) {
        this.localParameters = localParameters;
    }

    @Override
    public void execute(BuildCacheConfiguration buildCache) {
        if (localParameters.localBuildCacheDisabled()) {
            buildCache.local(enabled(set(false)));
        } else {
            buildCache.local(enabled(set(true)));
        }
        buildCache.local(new DisableCacheRemoval());
    }

    interface LocalBuildCacheParameters {
        boolean localBuildCacheDisabled();
    }

    static final class DisableCacheRemoval implements Action<DirectoryBuildCache> {
        @Override
        public void execute(DirectoryBuildCache buildCache) {
            buildCache.setRemoveUnusedEntriesAfterDays(Integer.MAX_VALUE);
        }
    }
}
