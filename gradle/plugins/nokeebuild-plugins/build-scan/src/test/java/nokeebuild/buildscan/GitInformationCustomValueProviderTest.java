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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitInformationCustomValueProviderTest {
	@Mock GitInformationCustomValueProvider.Parameters git;
	@InjectMocks GitInformationCustomValueProvider subject;
	@Mock BuildScanExtension buildScan;

	@Nested
	class GitStatusTest {
		@Test
		void tagsBuildScanWithDirtyTagWhenGitStatusIsNotEmpty() {
			when(git.gitStatus()).thenReturn(Optional.of("something"));
			subject.execute(buildScan);
			verify(buildScan).tag("dirty");
		}

		@Test
		void doesNotTagBuildScanWithDirtyTagWhenGitStatusIsEmpty() {
			when(git.gitStatus()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).tag("dirty");
		}

		@Test
		void addsGitStatusWhenNotEmpty() {
			when(git.gitStatus()).thenReturn(Optional.of("something something dirty"));
			subject.execute(buildScan);
			verify(buildScan).value("gitStatus", "something something dirty");
		}

		@Test
		void doesNotAddGitStatusWhenEmpty() {
			when(git.gitStatus()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).value(eq("gitStatus"), any());
		}
	}

	@Nested
	class GitRefTest {
		@Test
		void addsGitRefWhenPresent() {
			when(git.gitRef()).thenReturn(Optional.of("refs/heads/feature-branch-1"));
			subject.execute(buildScan);
			verify(buildScan).value("gitBranchName", "refs/heads/feature-branch-1");
		}

		@Test
		void doesNotAddsGitRefWhenNotPresent() {
			when(git.gitRef()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).value(eq("gitBranchName"), any());
		}
	}

	@Nested
	class GitCommitShaTest {
		@Test
		void addsCommitShaAsCustomValue() {
			when(git.gitCommitSha()).thenReturn(Optional.of("ffac537e6cbbf934b08745a378932722df287a53"));
			subject.execute(buildScan);
			verify(buildScan).value("gitCommitSha", "ffac537e6cbbf934b08745a378932722df287a53");
		}

		@Test
		void doesNotAddCommitShaAsCustomValueIfCommitShaAbsent() {
			when(git.gitCommitSha()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).value(eq("gitCommitSha"), any());
		}
	}

	@Nested
	class GitCommitIdTest {
		@Test
		void addsCommitIdAsCustomValue() {
			when(git.gitCommitId()).thenReturn(Optional.of("df287a53"));
			subject.execute(buildScan);
			verify(buildScan).value("gitCommitId", "df287a53");
		}

		@Test
		void doesNotAddCommitIdAsCustomValueIfCommitIdAbsent() {
			when(git.gitCommitSha()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).value(eq("gitCommitId"), any());
		}
	}

	@Nested
	class SourceUrlTest {
		@Test
		void addsSourceUrlToGitHubCommit() {
			when(git.gitCommitSha()).thenReturn(Optional.of("ffac537e6cbbf934b08745a378932722df287a53"));
			when(git.gitRepository()).thenReturn(Optional.of("https://github.com/octocat/Hello-World.git"));
			subject.execute(buildScan);
			verify(buildScan).link("Source", "https://github.com/octocat/Hello-World/commit/ffac537e6cbbf934b08745a378932722df287a53");
		}

		@Test
		void doesNotAddSourceUrlIfCommitShaAbsent() {
			when(git.gitCommitSha()).thenReturn(Optional.empty());
			when(git.gitRepository()).thenReturn(Optional.of("https://github.com/octocat/Hello-World.git"));
			subject.execute(buildScan);
			verify(buildScan, never()).link(eq("Source"), any());
		}

		@Test
		void doesNotAddSourceUrlIfRepositoryAbsent() {
			when(git.gitCommitSha()).thenReturn(Optional.of("ffac537e6cbbf934b08745a378932722df287a53"));
			when(git.gitRepository()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).link(eq("Source"), any());
		}
	}

	@Nested
	class BuildScanCommitSearchLinkTest {
		@Test
		void addsBuildScanSearchLinkToAllMatchingCommitId() {
			when(git.gitCommitSha()).thenReturn(Optional.of("ffac537e6cbbf934b08745a378932722df287a53"));
			when(buildScan.getServer()).thenReturn("https://my-company.gradle.com/");
			subject.execute(buildScan);
			verify(buildScan).link("Git Commit Scans", "https://my-company.gradle.com/scans?search.names=gitCommitSha&search.values=ffac537e6cbbf934b08745a378932722df287a53");
		}

		@Test
		void doesNotAddBuildScanSearchLinkToAllMatchingCommitIdWhenUsingPublicInstance() {
			when(git.gitCommitSha()).thenReturn(Optional.of("ffac537e6cbbf934b08745a378932722df287a53"));
			when(buildScan.getServer()).thenReturn(null);
			subject.execute(buildScan);
			verify(buildScan, never()).link(eq("Git Commit Scans"), any());
		}

		@Test
		void doesNotAddBuildScanSearchLinkToAllMatchingCommitIdWhenCommitShaNotAvailable() {
			when(git.gitCommitSha()).thenReturn(Optional.empty());
			subject.execute(buildScan);
			verify(buildScan, never()).link(eq("Git Commit Scans"), any());
		}
	}
}
