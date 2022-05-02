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

import javax.annotation.Nullable;

/**
 * <pre>
 * gradleEnterprise {
 *     buildScan {
 *         server = System.getProperty('gradle.enterprise.url', null)
 *     }
 * }
 * </pre>
 */
final class UseGradleEnterpriseBuildScanServerIfConfigured implements Action<BuildScanExtension> {
	public static final String GRADLE_ENTERPRISE_URL_PROPERTY_NAME = "gradle.enterprise.url";
	private final Parameters gradleEnterprise;

	public UseGradleEnterpriseBuildScanServerIfConfigured(Parameters gradleEnterprise) {
		this.gradleEnterprise = gradleEnterprise;
	}

	@Override
	public void execute(BuildScanExtension extension) {
		extension.setServer(gradleEnterprise.serverUrl());
	}

	interface Parameters {
		@Nullable
		String serverUrl();
	}
}
