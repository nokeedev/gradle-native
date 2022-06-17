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
package nokeedocs;

import dev.gradleplugins.dockit.dslref.AssembleDslDocTask;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;
import java.io.File;
import java.util.stream.Collectors;

import static net.nokeedev.jbake.JBakeExtension.jbake;

abstract class NokeeDocumentationModulePlugin implements Plugin<Project> {
	@Inject
	public NokeeDocumentationModulePlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.dsl-reference-module");

		project.getTasks().named("dsl", AssembleDslDocTask.class, task -> {
			task.getPluginsMetaDataFile().set(project.getLayout().getProjectDirectory().file("src/docs/dsl/plugins.xml"));
			task.getClassNames().set(project.provider(() -> {
				return project.getLayout().getProjectDirectory().dir("src/docs/dsl").getAsFileTree().matching(it -> it.include("*.json")).getFiles().stream().map(File::getName).map(FilenameUtils::removeExtension).collect(Collectors.toList());
			}));
			task.getClassDocbookDirectories().from(project.getLayout().getProjectDirectory().dir("src/docs/dsl"));
			task.getTemplateFile().set(project.project(":docs").file("src/docs/dsl/dsl.template"));
		});

		project.getPluginManager().withPlugin("net.nokeedev.jbake-site", ignored -> {
			jbake(project, extension -> {
				extension.getContent().from(extension.sync("dsl", spec -> {
					spec.into("dsl", it -> it.from(project.getTasks().named("dsl", AssembleDslDocTask.class).map(AssembleDslDocTask::getDestinationDirectory)));
				}));
			});
		});

		project.getPluginManager().apply("net.nokeedev.jbake-site");
	}
}
