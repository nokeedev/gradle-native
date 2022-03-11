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
package nokeebuild.buildscan;

import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;
import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfigureBuildScanExtensionTest {
    @Mock private ConfigureBuildScanExtension.Parameters parameters;
    @InjectMocks private ConfigureBuildScanExtension subject;
    @Mock private GradleEnterpriseExtension extension;

    @BeforeEach
    void setUp() {
        subject.execute(extension);
    }

    @Test
    void usesGradleEnterpriseServerUrl() {
        verify(extension).buildScan(isA(UseGradleEnterpriseBuildScanServerIfConfigured.class));
    }

    @Test
    void alwaysAgreeTermsOfServices() {
        verify(extension).buildScan(isA(AgreePublicBuildScanTermsOfService.class));
    }

    @Test
    void usesBackgroundUploadOnLocalBuildEnvironment() {
        verify(extension).buildScan(isA(UploadInBackgroundOnlyOnLocalBuildEnvironment.class));
    }

    @Test
    void usesIdeaIdeCustomValueProvider() {
        verify(extension).buildScan(valueProviderOf(IdeaIdeCustomValueProvider.class));
    }

    @Test
    void usesGitInformationCustomValueProvider() {
        verify(extension).buildScan(valueProviderOf(GitInformationCustomValueProvider.class));
    }

    @Test
    void usesBuildCacheCustomValueProvider() {
        verify(extension).buildScan(valueProviderOf(BuildCacheCustomValueProvider.class));
    }

    @Test
    void usesBuildEnvironmentCustomValueProvider() {
        verify(extension).buildScan(valueProviderOf(BuildCacheCustomValueProvider.class));
    }

    @Test
    void usesGitHubActionsCustomValueProvider() {
        verify(extension).buildScan(valueProviderOf(BuildCacheCustomValueProvider.class));
    }

    private static Action<BuildScanExtension> valueProviderOf(Class<? extends Action<BuildScanExtension>> providerType) {
        return argThat(new CustomValueProvider(providerType));
    }

    private static final class CustomValueProvider implements ArgumentMatcher<Action<BuildScanExtension>> {
        private final Class<? extends Action<BuildScanExtension>> providerType;

        public CustomValueProvider(Class<? extends Action<BuildScanExtension>> providerType) {
            this.providerType = providerType;
        }

        @Override
        public boolean matches(Action<BuildScanExtension> argument) {
            if (argument instanceof InBackground) {
                return providerType.isInstance(((InBackground) argument).getDelegate());
            } else {
                return providerType.isInstance(argument);
            }
        }
    }
}
