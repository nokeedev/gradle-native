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

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.gradleplugins.dockit.javadoc.JavadocSourcePathsOption.sourcePaths;
import static dev.gradleplugins.dockit.javadoc.JavadocSourcesOption.sources;
import static dev.gradleplugins.dockit.javadoc.JavadocTaskUtils.ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption;
import static java.util.stream.Collectors.toList;

abstract class JavadocApiReferencePlugin implements Plugin<Project> {
	private final ObjectFactory objects;

	@Inject
	public JavadocApiReferencePlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.javadoc-base");

		final NamedDomainObjectProvider<Configuration> apiReferenceSource = project.getConfigurations().register("apiReferenceSource", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(true);
			configuration.attributes(attributes -> {
				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "api-reference-sources"));
			});
			configuration.getDependencies().addAll(allProjects(project));
		});

		final NamedDomainObjectProvider<Configuration> apiReferenceClasspath = project.getConfigurations().register("apiReferenceClasspath", configuration -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(true);
			configuration.getDependencies().addAllLater(objects.listProperty(Dependency.class).value(apiReferenceSource.map(allResolvedProjects()).map(asProjectDependencies(project))));
		});

		project.getTasks().register("apiReferenceJavadoc", Javadoc.class, task -> {
			Provider<ArtifactCollection> apiReferenceSourceArtifacts = apiReferenceSource.map(it -> it.getIncoming().artifactView(view -> view.lenient(true)).getArtifacts());
			task.setSource(ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(task));
			sources(task).from(apiReferenceSourceArtifacts.flatMap(elementsOf(ArtifactCollection::getArtifactFiles)));
			task.setClasspath(finalizeValueOnRead(objects.fileCollection().from(apiReferenceClasspath.flatMap(elementsOf(it -> it.getIncoming().getFiles())))));
			sourcePaths(task).from(apiReferenceSourceArtifacts.flatMap(elementsOf(ArtifactCollection::getArtifactFiles))).finalizeValueOnRead();
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("api-reference-javadoc").get().getAsFile());
		});
	}

	private static Collection<Dependency> allProjects(Project project) {
		return project.getRootProject().getAllprojects().stream()
			.filter(it -> !it.equals(project))
			.map(project.getDependencies()::create)
			.collect(toList());
	}

	private static Transformer<Collection<String>, Configuration> allResolvedProjects() {
		return configuration -> configuration.getIncoming().artifactView(it -> it.lenient(true)).getArtifacts().getArtifacts().stream()
			.map(it -> it.getVariant().getOwner())
			.flatMap(onlyProjectComponentIdentifier())
			.map(ProjectComponentIdentifier::getProjectPath)
			.distinct()
			.collect(toList());
	}

	private static Function<ComponentIdentifier, Stream<ProjectComponentIdentifier>> onlyProjectComponentIdentifier() {
		return identifier -> {
			if (identifier instanceof ProjectComponentIdentifier) {
				return Stream.of((ProjectComponentIdentifier) identifier);
			} else {
				return Stream.empty();
			}
		};
	}

	private static Transformer<Collection<Dependency>, Collection<String>> asProjectDependencies(Project project) {
		return projectPaths -> projectPaths.stream().map(it -> {
			return project.getDependencies().project(new HashMap<String, Object>() {{
				put("path", it);
				put("configuration", "apiReferenceElements");
			}});
		}).collect(toList());
	}

	static <IN> Transformer<Provider<Set<FileSystemLocation>>, IN> elementsOf(Transformer<FileCollection, IN> mapper) {
		return it -> mapper.transform(it).getElements();
	}

	private static <T extends HasConfigurableValue> T finalizeValueOnRead(T self) {
		self.finalizeValueOnRead();
		return self;
	}
}
