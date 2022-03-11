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

import static nokeebuild.buildscan.GradleEnterpriseCustomSearchQueryUrlTransformer.toCustomSearchUrl;

final class GitHubActionsCustomValueProvider implements Action<BuildScanExtension> {
    private static final String BUILD_ID = "buildId";
    private static final String GIT_COMMIT_NAME = "gitCommitId";
    private final GitHubActionsParameters gitHubActions;

    public GitHubActionsCustomValueProvider(GitHubActionsParameters gitHubActions) {
        this.gitHubActions = gitHubActions;
    }

    @Override
    public void execute(BuildScanExtension buildScan) {
        if (gitHubActions.isGitHubActionsEnvironment()) {
            String commitId = gitHubActions.githubSha();
            String repo = gitHubActions.githubRepository();
            String runId = gitHubActions.githubRunId();
            String buildUrl = String.format("https://github.com/%s/actions/runs/%s", repo, runId);
            String commitUrl = String.format("https://github.com/%s/commit/%s", repo, commitId);

            buildScan.link("GitHub Actions Build", buildUrl);
            buildScan.link("Source", commitUrl);
            buildScan.value(BUILD_ID, String.format("%s %s", runId, gitHubActions.githubRunNumber()));
            buildScan.value(GIT_COMMIT_NAME, commitId);

            Optional.ofNullable(buildScan.getServer()).map(toCustomSearchUrl(Collections.singletonMap(GIT_COMMIT_NAME, commitId))).ifPresent(url -> buildScan.link("Git Commit Scans", url));
        }
    }

//    @SuppressWarnings("UnstableApiUsage")
//    private boolean isGitHubActionsEnvironment() {
//        return providers.environmentVariable("GITHUB_ACTIONS").forUseAtConfigurationTime().isPresent();
//    }
//
//    private String envVar(String variableName) {
//        return providers.environmentVariable(variableName).forUseAtConfigurationTime().orElse("").get();
//    }

    interface GitHubActionsParameters {
        boolean isGitHubActionsEnvironment();
        String githubSha();
        String githubRepository();
        String githubRunId();
        String githubRunNumber();
    }
}
