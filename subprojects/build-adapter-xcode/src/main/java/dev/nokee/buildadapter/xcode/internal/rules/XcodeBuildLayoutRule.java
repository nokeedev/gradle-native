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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.nokee.buildadapter.xcode.internal.GradleBuildLayout;
import dev.nokee.buildadapter.xcode.internal.GradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectElement;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodebuildExecTask;
import dev.nokee.model.internal.buffers.ModelBufferComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.function.Function;

import static dev.nokee.buildadapter.xcode.internal.plugins.HasWorkingDirectory.workingDirectory;
import static dev.nokee.buildadapter.xcode.internal.plugins.XcodeBuildAdapterPlugin.forXcodeProject;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.utils.ActionUtils.composite;

public class XcodeBuildLayoutRule extends ModelActionWithInputs.ModelAction1<ModelBufferComponent<XCProjectElement>> {
	private final GradleProjectPathService projectPaths;
	private final GradleBuildLayout buildLayout;
	private final ProviderFactory providers;

	public XcodeBuildLayoutRule(GradleProjectPathService projectPaths, GradleBuildLayout buildLayout, ProviderFactory providers) {
		this.projectPaths = projectPaths;
		this.buildLayout = buildLayout;
		this.providers = providers;
	}

	@Override
	protected void execute(ModelNode entity, ModelBufferComponent<XCProjectElement> projects) {
		Streams.stream(projects).map(XCProjectElement::get).collect(ImmutableMap.toImmutableMap(projectPaths::toProjectPath, Function.identity())).forEach((path, reference) -> {
			buildLayout.include(path);
			buildLayout.project(path, project -> {
				project.getPluginManager().apply("dev.nokee.model-base");
				forXcodeProject(reference, composite(
					workingDirectory(set(project.getRootProject().getLayout().getProjectDirectory())),
					(XcodebuildExecTask task) -> task.getSdk().set(fromCommandLine("sdk")),
					(XcodebuildExecTask task) -> task.getConfiguration().set(fromCommandLine("configuration"))
				)).execute(project);
			});
		});
	}

	private Provider<String> fromCommandLine(String name) {
		return providers.systemProperty(name).orElse(providers.gradleProperty(name));
	}
}
