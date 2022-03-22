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

import java.util.Optional;

final class GitInformationCustomValueProvider implements Action<BuildScanExtension> {
	private static final String GIT_STATUS = "gitStatus";
	private static final String GIT_BRANCH_NAME = "gitBranchName";
	private final GitInformationParameters gitInformation;

	GitInformationCustomValueProvider(GitInformationParameters gitInformation) {
		this.gitInformation = gitInformation;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		gitInformation.gitStatus().ifPresent(it -> {
			buildScan.tag("dirty");
			buildScan.value(GIT_STATUS, it);
		});
		gitInformation.gitRef().ifPresent(it -> buildScan.value(GIT_BRANCH_NAME, it));
	}

	interface GitInformationParameters {
		Optional<String> gitRef();

		Optional<String> gitStatus();
	}
}
