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

import dev.nokee.buildadapter.xcode.internal.components.XCWorkspaceComponent;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodebuildExecTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import static dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterPlugin.forXcodeWorkspace;
import static dev.nokee.utils.ActionUtils.composite;

public final class XcodeWorkspaceRule extends ModelActionWithInputs.ModelAction1<XCWorkspaceComponent> {
	private final Settings settings;
	private final ProviderFactory providers;

	public XcodeWorkspaceRule(Settings settings, ProviderFactory providers) {
		this.settings = settings;
		this.providers = providers;
	}

	@Override
	protected void execute(ModelNode entity, XCWorkspaceComponent workspace) {
		settings.getGradle().rootProject(forXcodeWorkspace(workspace.get().load(), composite(
			(XcodebuildExecTask task) -> task.getSdk().set(fromCommandLine("sdk")),
			(XcodebuildExecTask task) -> task.getConfiguration().set(fromCommandLine("configuration"))
		)));
	}

	private Provider<String> fromCommandLine(String name) {
		return providers.systemProperty(name).orElse(providers.gradleProperty(name));
	}
}
