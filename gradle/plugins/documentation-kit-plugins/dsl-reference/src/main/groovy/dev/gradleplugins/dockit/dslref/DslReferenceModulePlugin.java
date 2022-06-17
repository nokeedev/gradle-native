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
package dev.gradleplugins.dockit.dslref;

import dev.gradleplugins.dockit.dslref.AssembleDslDocTask;
import dev.gradleplugins.dockit.dslref.ExtractDslMetaDataTask;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

abstract class DslReferenceModulePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public DslReferenceModulePlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
//		final TaskProvider<Sync> syncTask = project.getTasks().register("syncDslReferenceSourceElements", Sync.class, task -> {
//			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
//		});
//
//		project.getConfigurations().register("dslReferenceSourceElements", configuration -> {
//			configuration.setCanBeConsumed(true);
//			configuration.setCanBeResolved(false);
//			configuration.attributes(attributes -> {
//				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
//				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
//				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "dsl-reference-sources"));
//			});
//			configuration.getOutgoing().artifact(syncTask.map(Sync::getDestinationDir), it -> {
//				it.setType("directory");
//				it.builtBy(syncTask);
//			});
//		});
//
//		project.getPluginManager().withPlugin("java", __ -> {
//			Provider<SourceSet> mainSourceSet = sourceSets(project).flatMap(it -> it.named("main"));
//
//			project.getConfigurations().register("dslReferenceElements", configuration -> {
//				configuration.setCanBeConsumed(true);
//				configuration.setCanBeResolved(false);
//				configuration.getDependencies().addLater(mainSourceSet.map(it -> project.getDependencies().create(project.files(it.getCompileClasspath().getFiles()))));
//			});
//
//			syncTask.configure(task -> {
//				task.from(mainSourceSet.map(it -> it.getAllSource()));
//			});
//		});
//
//		final NamedDomainObjectProvider<Configuration> apiReferenceSource = project.getConfigurations().register("apiReferenceSource", configuration -> {
//			configuration.setCanBeConsumed(false);
//			configuration.setCanBeResolved(true);
//			configuration.attributes(attributes -> {
//				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
//				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
//				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "api-reference-sources"));
//			});
//			configuration.getDependencies().addAll(allProjects(project));
//		});

		final TaskProvider<ExtractDslMetaDataTask> extractTask = project.getTasks().register("extractDslMetaData", ExtractDslMetaDataTask.class, task -> {
			task.getDestinationFile().set(project.getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/dslMetaData.bin"));
		});

		final NamedDomainObjectProvider<Configuration> dslMetaDataElements = project.getConfigurations().register("dslMetaDataElements", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "dsl-meta-data"));
			});
			configuration.getOutgoing().artifact(extractTask.flatMap(ExtractDslMetaDataTask::getDestinationFile));
		});

		final NamedDomainObjectProvider<Configuration> dslMetaData = project.getConfigurations().register("dslMetaData", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(true);
			configuration.attributes(attributes -> {
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "dsl-meta-data"));
			});
		});

		project.getPluginManager().withPlugin("java", __ -> {
			Provider<SourceSet> mainSourceSet = sourceSets(project).flatMap(it -> it.named("main"));
			extractTask.configure(task -> task.setSource(mainSourceSet.flatMap(elementsOf(SourceSet::getAllSource))));

			dslMetaDataElements.configure(configuration -> {
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getCompileOnlyConfigurationName).flatMap(project.getConfigurations()::named).get());
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getCompileOnlyApiConfigurationName).flatMap(project.getConfigurations()::named).get());
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getImplementationConfigurationName).flatMap(project.getConfigurations()::named).get());
			});

			dslMetaData.configure(configuration -> {
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getCompileOnlyConfigurationName).flatMap(project.getConfigurations()::named).get());
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getCompileOnlyApiConfigurationName).flatMap(project.getConfigurations()::named).get());
				configuration.extendsFrom(mainSourceSet.map(SourceSet::getImplementationConfigurationName).flatMap(project.getConfigurations()::named).get());
			});
		});

		project.getTasks().register("dsl", AssembleDslDocTask.class, task -> {
			task.getClassMetaDataFiles().from(extractTask.flatMap(ExtractDslMetaDataTask::getDestinationFile));
			task.getClassMetaDataFiles().from(dslMetaData.flatMap(elementsOf(it -> it.getIncoming().artifactView(t -> t.lenient(true)).getArtifacts().getArtifactFiles())));
//			task.getPluginsMetaDataFile().convention(project.getLayout().getProjectDirectory().file("src/docs/dsl/plugins.xml"));
//
//			task.getClassNames().set(project.provider(() -> {
//				return project.getLayout().getProjectDirectory().dir("src/docs/dsl").getAsFileTree().matching(it -> it.include("*.json")).getFiles().stream().map(File::getName).map(FilenameUtils::removeExtension).collect(Collectors.toList());
//			}));
//			task.getClassDocbookDirectories().from(project.getLayout().getProjectDirectory().dir("src/docs/dsl"));
//			task.getTemplateFile().set(project.project(":docs").file("src/docs/dsl/dsl.template"));
			task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()));
		});
	}

	private static Provider<SourceSetContainer> sourceSets(Project project) {
		return project.provider(() -> (SourceSetContainer) project.getExtensions().getByName("sourceSets"));
	}

	private static <IN> Transformer<Provider<Set<FileSystemLocation>>, IN> elementsOf(Transformer<FileCollection, IN> mapper) {
		return it -> mapper.transform(it).getElements();
	}
}
