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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseLocalBuildCacheTest {
    @Mock private UseLocalBuildCache.LocalBuildCacheParameters parameters;
    @InjectMocks private UseLocalBuildCache subject;
    private final BuildCacheConfiguration buildCache = BuildCacheConfigurationTestUtils.mock();

    @Test
    void canDisableLocalBuildCacheUsingParameters() {
        when(parameters.localBuildCacheDisabled()).thenReturn(true);

        subject.execute(buildCache);
        assertFalse(buildCache.getLocal().isEnabled());
    }

    @Test
    void localBuildCacheEnabledByDefault() {
        subject.execute(buildCache);
        assertTrue(buildCache.getLocal().isEnabled());
    }

    @Test
    void disableCacheRemovalByDefault() {
        subject.execute(buildCache);
        assertEquals(Integer.MAX_VALUE, buildCache.getLocal().getRemoveUnusedEntriesAfterDays());
    }
}
