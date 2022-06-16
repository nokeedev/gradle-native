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
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.inject.Inject;
import java.util.Collection;
import java.util.concurrent.Callable;

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
			configuration.attributes(attributes -> {
				attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, "java-api"));
//				attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.DOCUMENTATION));
//				attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.class, Bundling.EXTERNAL));
//				attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.class, "api-reference-sources"));
			});
			configuration.getDependencies().addAll(allProjects(project));
		});

		project.getTasks().register("apiReferenceJavadoc", Javadoc.class, task -> {
			task.setSource(ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(task));
			sources(task).from(apiReferenceSource.flatMap(it -> it.getIncoming().artifactView(view -> view.lenient(true)).getArtifacts().getArtifactFiles().getElements()));
			task.setClasspath(objects.fileCollection().from(apiReferenceClasspath.flatMap(it -> {
				return it.getIncoming().artifactView(view -> view.lenient(true)).getArtifacts().getArtifactFiles().getElements();
			})));
			sourcePaths(task).from(apiReferenceSource.flatMap(it -> {
				return it.getIncoming().artifactView(view -> view.lenient(true)).getArtifacts().getArtifactFiles().getElements();
			})).finalizeValueOnRead();
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("api-reference-javadoc").get().getAsFile());
		});
	}

	private static Collection<Dependency> allProjects(Project project) {
		return project.getRootProject().getAllprojects().stream().filter(it -> !it.equals(project)).peek(System.out::println).map(project.getDependencies()::create).collect(toList());
	}

	private static <T> Callable<T> callableOf(Callable<T> delegate) {
		return new Callable<T>() {
			private transient volatile boolean initialized;
			private transient T value;

			@Override
			public T call() throws Exception {
				// A 2-field variant of Double Checked Locking.
				if (!initialized) {
					synchronized (this) {
						if (!initialized) {
							T t = delegate.call();
							value = t;
							initialized = true;
							return t;
						}
					}
				}
				// This is safe because we checked `initialized.`
				return value;
			}
		};
	}
}
