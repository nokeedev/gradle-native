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
import org.gradle.api.Action;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nokeebuild.buildscan.GradleEnterpriseCustomSearchQueryUrlTransformer.toCustomSearchUrl;

final class GitInformationCustomValueProvider implements Action<BuildScanExtension> {
	private static final String GIT_STATUS = "gitStatus";
	private static final String GIT_BRANCH_NAME = "gitBranchName";
	static final String GIT_COMMIT_NAME = "gitCommitId";
	private final Parameters gitInformation;

	GitInformationCustomValueProvider(Parameters gitInformation) {
		this.gitInformation = gitInformation;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		gitInformation.gitStatus().ifPresent(it -> {
			buildScan.tag("dirty");
			buildScan.value(GIT_STATUS, it);
		});
		gitInformation.gitRef().ifPresent(it -> buildScan.value(GIT_BRANCH_NAME, it));
		gitInformation.gitCommitSha().ifPresent(it -> buildScan.value(GIT_COMMIT_NAME, it));

		ifBothPresent(gitInformation.gitRepository(), gitInformation.gitCommitSha(), (gitRepo, gitSha) -> {
			if (gitRepo.contains("github.com/") || gitRepo.contains("github.com:")) {
				Matcher matcher = Pattern.compile("(.*)github\\.com[/|:](.*)").matcher(gitRepo);
				if (matcher.matches()) {
					String rawRepoPath = matcher.group(2);
					String repoPath = rawRepoPath.endsWith(".git") ? rawRepoPath.substring(0, rawRepoPath.length() - 4) : rawRepoPath;
					buildScan.link("Source", String.format("https://github.com/%s/commit/%s", repoPath, gitSha));
				}
			}
		});

		gitInformation.gitCommitSha().ifPresent(commitId ->
			Optional.ofNullable(buildScan.getServer()).map(toCustomSearchUrl(Collections.singletonMap(GIT_COMMIT_NAME, commitId))).ifPresent(url -> buildScan.link("Git Commit Scans", url)));
	}

	private static <T, U> void ifBothPresent(Optional<T> first, Optional<U> second, BiConsumer<? super T, ? super U> action) {
		if (first.isPresent() && second.isPresent()) {
			action.accept(first.get(), second.get());
		}
	}

	interface Parameters {
		Optional<String> gitRef();

		Optional<String> gitStatus();

		Optional<String> gitCommitSha();

		Optional<String> gitRepository();
	}
}
