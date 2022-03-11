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

import com.gradle.scan.plugin.BuildScanExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubActionsCustomValueProviderTest {
    @Mock private GitHubActionsCustomValueProvider.GitHubActionsParameters gitHubActions;
    @InjectMocks private GitHubActionsCustomValueProvider subject;
    @Mock private BuildScanExtension buildScan;

    @BeforeEach
    void setUp() {
        when(gitHubActions.isGitHubActionsEnvironment()).thenReturn(false);
    }

    @Test
    void doesNotIncludeAnyTagsOrValuesOrLinksWhenNotInGitHubActionsEnvironment() {
        subject.execute(buildScan);
        verify(buildScan, never()).tag(any());
        verify(buildScan, never()).value(any(), any());
    }

    @Nested
    class WhenGitHubActionsEnvironmentTest {
        @BeforeEach
        void setUp() {
            when(gitHubActions.isGitHubActionsEnvironment()).thenReturn(true);
            when(gitHubActions.githubRepository()).thenReturn("octocat/Hello-World");
            when(gitHubActions.githubRunId()).thenReturn("1658821493");
            when(gitHubActions.githubSha()).thenReturn("ffac537e6cbbf934b08745a378932722df287a53");
            when(gitHubActions.githubRunNumber()).thenReturn("3");
        }

        @Test
        void addsBuildUrlToActions() {
            subject.execute(buildScan);
            verify(buildScan).link("GitHub Actions Build", "https://github.com/octocat/Hello-World/actions/runs/1658821493");
        }

        @Test
        void addsSourceUrlToCommit() {
            subject.execute(buildScan);
            verify(buildScan).link("Source", "https://github.com/octocat/Hello-World/commit/ffac537e6cbbf934b08745a378932722df287a53");
        }

        @Test
        void addsBuildIdAsCustomValue() {
            subject.execute(buildScan);
            verify(buildScan).value("buildId", "1658821493 3");
        }

        @Test
        void addsCommitIdAsCustomValue() {
            subject.execute(buildScan);
            verify(buildScan).value("gitCommitId", "ffac537e6cbbf934b08745a378932722df287a53");
        }

        @Test
        void addsBuildScanSearchLinkToAllMatchingCommitId() {
            when(buildScan.getServer()).thenReturn("https://my-company.gradle.com/");
            subject.execute(buildScan);
            verify(buildScan).link("Git Commit Scans", "https://my-company.gradle.com//scans?search.names=gitCommitId&search.values=ffac537e6cbbf934b08745a378932722df287a53");
        }

        @Test
        void doesNotAddBuildScanSearchLinkToAllMatchingCommitIdWhenUsingPublicInstance() {
            when(buildScan.getServer()).thenReturn(null);
            subject.execute(buildScan);
            verify(buildScan, never()).link(eq("Git Commit Scans"), any());
        }
    }
}
