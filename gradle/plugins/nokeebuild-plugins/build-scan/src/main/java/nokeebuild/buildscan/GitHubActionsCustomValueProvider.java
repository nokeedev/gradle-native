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

final class GitHubActionsCustomValueProvider implements Action<BuildScanExtension> {
	private static final String BUILD_ID = "buildId";
	private final Parameters gitHubActions;

	public GitHubActionsCustomValueProvider(Parameters gitHubActions) {
		this.gitHubActions = gitHubActions;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		if (gitHubActions.isGitHubActionsEnvironment()) {
			String repo = gitHubActions.githubRepository();
			String runId = gitHubActions.githubRunId();
			String buildUrl = String.format("https://github.com/%s/actions/runs/%s", repo, runId);

			buildScan.link("GitHub Actions Build", buildUrl);
			buildScan.value(BUILD_ID, String.format("%s %s", runId, gitHubActions.githubRunNumber()));
		}
	}

	interface Parameters {
		boolean isGitHubActionsEnvironment();

		String githubRepository();

		String githubRunId();

		String githubRunNumber();
	}
}
