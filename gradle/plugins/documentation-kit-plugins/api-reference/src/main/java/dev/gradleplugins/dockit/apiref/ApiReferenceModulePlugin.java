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
package dev.gradleplugins.dockit.apiref;

import org.apache.tools.ant.taskdefs.optional.depend.Depend;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class ApiReferenceModulePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public ApiReferenceModulePlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		final TaskProvider<Sync> syncTask = project.getTasks().register("syncApiReferenceSourceElements", Sync.class, task -> {
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
		});

		project.getConfigurations().register("apiReferenceSourceElements", configuration -> {
			configuration.setCanBeConsumed(true);
			configuration.setCanBeResolved(false);
			configuration.attributes(attributes -> {
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "api-reference-sources"));
			});
			configuration.getOutgoing().artifact(syncTask.map(Sync::getDestinationDir), it -> {
				it.setType("directory");
				it.builtBy(syncTask);
			});
		});

		project.getPluginManager().withPlugin("java", __ -> {
			Provider<SourceSet> mainSourceSet = sourceSets(project).flatMap(it -> it.named("main"));
			project.getConfigurations().register("apiReferenceElements", configuration -> {
				configuration.setCanBeConsumed(true);
				configuration.setCanBeResolved(false);
				configuration.getDependencies().addAllLater(mainSourceSet.map(SourceSet::getImplementationConfigurationName).map(project.getConfigurations()::getByName).map(this::allDep));
				configuration.getDependencies().addAllLater(mainSourceSet.map(SourceSet::getCompileOnlyApiConfigurationName).map(project.getConfigurations()::getByName).map(this::allDep));
				configuration.getDependencies().addAllLater(mainSourceSet.map(SourceSet::getCompileOnlyConfigurationName).map(project.getConfigurations()::getByName).map(this::allDep));
				configuration.attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "java-api"));
				});
			});

			syncTask.configure(task -> {
				task.from(mainSourceSet.map(it -> it.getAllSource()));
			});
		});
	}

	private Iterable<Dependency> allDep(Configuration it) {
		return it.getDependencies().stream().map(dependency -> {
			final Dependency result = dependency.copy();
			if (result instanceof ModuleDependency) {
				((ModuleDependency) result).attributes(attributes -> {
					attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_API));
					attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.class, LibraryElements.CLASSES));
				});
			}
			return result;
		}).collect(Collectors.toList());
	}

	private static Provider<SourceSetContainer> sourceSets(Project project) {
		return project.provider(() -> (SourceSetContainer) project.getExtensions().getByName("sourceSets"));
	}
}
