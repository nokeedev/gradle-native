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
package dev.nokee.ide.visualstudio.internal.tasks;

import com.google.common.collect.ImmutableMap;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeGuid;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringSubstitutor;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenerateVisualStudioIdeSolutionTask extends DefaultTask {
	@Internal
	public abstract SetProperty<VisualStudioIdeProjectReference> getProjectReferences();

	@InputFiles
	protected Provider<List<FileSystemLocation>> getInputFiles() {
		return getProjectReferences().map(it -> it.stream().map(VisualStudioIdeProjectReference::getProjectLocation).map(Provider::get).collect(Collectors.toList()));
	}

	@OutputFile
	public abstract RegularFileProperty getSolutionLocation();

	@Inject
	public GenerateVisualStudioIdeSolutionTask() {
		dependsOn(getProjectReferences());
	}

	@TaskAction
	private void doGenerate() throws FileNotFoundException {

		try (PrintWriter out = new PrintWriter(getSolutionLocation().get().getAsFile())) {
			out.println("Microsoft Visual Studio Solution File, Format Version 12.00");
			out.println("# Visual Studio Version 16");
			out.println("VisualStudioVersion = 16.0.30225.117");
			out.println("MinimumVisualStudioVersion = 10.0.40219.1");
			getProjectReferences().get().forEach(reference -> {
				out.println("Project(\"{8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942}\") = \"" + FilenameUtils.removeExtension(reference.getProjectLocation().get().getAsFile().getName()) + "\", \"" + reference.getProjectLocation().get().getAsFile().getAbsolutePath() + "\", \"" + reference.getProjectGuid().get().toString() + "\"");
				out.println("EndProject");
			});
			out.println("Global");
			out.println("	GlobalSection(SolutionConfigurationPlatforms) = preSolution");
			getProjectReferences().get().stream().flatMap(it -> it.getProjectConfigurations().get().stream()).distinct().forEach(projectConfiguration -> {
				val valuesMap = ImmutableMap.<String, Object>builder()
					.put("configurationName", projectConfiguration.getConfiguration().getIdentifier())
					.put("platformName", projectConfiguration.getPlatform().getIdentifier())
					.build();
				out.println(new StringSubstitutor(valuesMap).replace("		${configurationName}|${platformName} = ${configurationName}|${platformName}"));
			});
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(ProjectConfigurationPlatforms) = postSolution");
			getProjectReferences().get().forEach(info -> {
				val firstProjectConfiguration = info.getProjectConfigurations().get().iterator().next();
				val valuesMap = ImmutableMap.<String, Object>builder()
					.put("uuid", info.getProjectGuid().get().toString())
					.put("configurationName", firstProjectConfiguration.getConfiguration().getIdentifier())
					.put("platformName", firstProjectConfiguration.getPlatform().getIdentifier())
					.build();
				out.println(new StringSubstitutor(valuesMap).replace("		${uuid}.${configurationName}|${platformName}.ActiveCfg = ${configurationName}|${platformName}"));
				int index = 0;
				for (VisualStudioIdeProjectConfiguration projectConfiguration : info.getProjectConfigurations().get()) {
					val values = ImmutableMap.<String, Object>builder()
						.put("uuid", info.getProjectGuid().get().toString())
						.put("configurationName", projectConfiguration.getConfiguration().getIdentifier())
						.put("platformName", projectConfiguration.getPlatform().getIdentifier())
						.put("index", index++)
						.build();
					out.println(new StringSubstitutor(values).replace("		${uuid}.${configurationName}|${platformName}.Build.${index} = ${configurationName}|${platformName}"));
				}
			});
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(SolutionProperties) = preSolution");
			out.println("		HideSolutionNode = FALSE");
			out.println("	EndGlobalSection");
			out.println("	GlobalSection(ExtensibilityGlobals) = postSolution");
			out.println(String.format("		SolutionGuid = %s", DefaultVisualStudioIdeGuid.stableGuidFrom(getSolutionLocation()).getAsString()));
			out.println("	EndGlobalSection");
			out.println("EndGlobal");
		}
	}
}
