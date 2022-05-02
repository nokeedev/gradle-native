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
import org.gradle.api.Action;

final class ConfigureBuildScanExtension implements Action<GradleEnterpriseExtension> {
	private final Parameters parameters;

	public ConfigureBuildScanExtension(Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public void execute(GradleEnterpriseExtension extension) {
		extension.buildScan(new UseGradleEnterpriseBuildScanServerIfConfigured(parameters));
		extension.buildScan(new AgreePublicBuildScanTermsOfService(parameters));
		extension.buildScan(new UploadInBackgroundOnlyOnLocalBuildEnvironment(() -> parameters.buildEnvironment() == BuildEnvironmentCustomValueProvider.BuildEnvironment.LOCAL));
		extension.buildScan(new AlwaysPublishBuildScan());
		extension.buildScan(new CaptureTaskInputFiles());

		// Custom value providers
		extension.buildScan(new BuildEnvironmentCustomValueProvider(parameters));
		extension.buildScan(new IdeaIdeCustomValueProvider(parameters));
		extension.buildScan(new GitHubActionsCustomValueProvider(parameters));
		extension.buildScan(new BuildCacheCustomValueProvider(parameters));
		extension.buildScan(new InBackground(new GitInformationCustomValueProvider(parameters)));
	}

	interface Parameters extends BuildCacheCustomValueProvider.Parameters
		, BuildEnvironmentCustomValueProvider.Parameters
		, IdeaIdeCustomValueProvider.Parameters
		, UseGradleEnterpriseBuildScanServerIfConfigured.Parameters
		, GitHubActionsCustomValueProvider.Parameters
		, GitInformationCustomValueProvider.Parameters
		, AgreePublicBuildScanTermsOfService.Parameters {
	}
}
