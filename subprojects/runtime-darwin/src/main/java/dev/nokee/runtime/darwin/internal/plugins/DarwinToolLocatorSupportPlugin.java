/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.runtime.darwin.internal.plugins;

import dev.nokee.runtime.base.internal.plugins.ToolResolutionBasePlugin;
import dev.nokee.runtime.base.internal.repositories.NokeeServerService;
import dev.nokee.runtime.darwin.internal.locators.XcodeLocator;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.runtime.base.internal.plugins.FakeMavenRepositoryPlugin.NOKEE_SERVER_SERVICE_NAME;

public class DarwinToolLocatorSupportPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ToolResolutionBasePlugin.class);

		NokeeServerService.Parameters parameters = (NokeeServerService.Parameters)project.getGradle().getSharedServices().getRegistrations().getByName(NOKEE_SERVER_SERVICE_NAME).getParameters();
		parameters.getToolLocators().add(XcodeLocator.class.getCanonicalName());
	}
}
