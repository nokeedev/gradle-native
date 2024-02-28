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
package dev.nokee.buildadapter.xcode.internal.rules;

import dev.nokee.buildadapter.xcode.internal.GradleBuildLayout;
import dev.nokee.buildadapter.xcode.internal.plugins.CurrentXcodeInstallationValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterExtension;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeInstallation;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodebuildExecTask;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import static dev.nokee.buildadapter.xcode.internal.plugins.HasWorkingDirectory.workingDirectory;
import static dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterPlugin.forXcodeProject;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.utils.ActionUtils.composite;
import static dev.nokee.utils.ProviderUtils.disallowChanges;
import static dev.nokee.utils.ProviderUtils.finalizeValueOnRead;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;

public final class XcodeBuildLayoutRule implements Action<XcodeBuildAdapterExtension.XCProjectExtension> {
	private final GradleBuildLayout buildLayout;
	private final ProviderFactory providers;
	private final Provider<XcodeInstallation> defaultXcodeInstallation;

	public XcodeBuildLayoutRule(GradleBuildLayout buildLayout, ProviderFactory providers, ObjectFactory objects) {
		this.buildLayout = buildLayout;
		this.providers = providers;
		this.defaultXcodeInstallation = finalizeValueOnRead(disallowChanges(objects.property(XcodeInstallation.class).value(providers.of(CurrentXcodeInstallationValueSource.class, ActionUtils.doNothing()))));
	}

	@Override
	public void execute(XcodeBuildAdapterExtension.XCProjectExtension extension) {
		buildLayout.include(extension.getProjectPath().get());
		buildLayout.project(extension.getProjectPath().get(), project -> {
			project.getPluginManager().apply("dev.nokee.model-base");
			forXcodeProject(extension.getProjectLocation().get(), composite(
				workingDirectory(set(project.getRootProject().getLayout().getProjectDirectory())),
				(XcodebuildExecTask task) -> task.getSdk().set(fromCommandLine("sdk")),
				(XcodebuildExecTask task) -> task.getXcodeInstallation().set(defaultXcodeInstallation)
			)).execute(project);
		});
	}

	private Provider<String> fromCommandLine(String name) {
		// TODO: I'm not convince forUseAtConfigurationTime is required here
		return forUseAtConfigurationTime(providers.systemProperty(name)).orElse(forUseAtConfigurationTime(providers.gradleProperty(name)));
	}
}
