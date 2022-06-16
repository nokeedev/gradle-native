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
package nokeebuild;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static dev.gradleplugins.dockit.javadoc.JavadocLinksOption.links;
import static dev.gradleplugins.dockit.javadoc.JavadocSourcePathsOption.sourcePaths;
import static dev.gradleplugins.dockit.javadoc.JavadocSourcesOption.sources;
import static dev.gradleplugins.dockit.javadoc.JavadocTaskUtils.ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption;

final class JavadocGradleDevelopmentConvention implements Action<Javadoc> {
	private final Project project;

	public JavadocGradleDevelopmentConvention(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Javadoc task) {
		task.setSource(ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(task));

		sources(task).from(callableOf(this::pluginSourceFiles));
		links(task).addAll(compatibility(gradlePlugin(project)).getMinimumGradleVersion().map(version -> {
			return Arrays.asList(
				project.uri("https://docs.oracle.com/javase/" + minimumJavaVersionFor(version).getMajorVersion() + "/docs/api"),
				project.uri("https://docs.gradle.org/" + version + "/javadoc/")
			);
		}));
		sourcePaths(task).from(callableOf(this::pluginSourceDirectories));
	}

	private FileTree pluginSourceFiles() {
		return gradlePlugin(project).getPluginSourceSet().getAllJava();
	}

	private FileCollection pluginSourceDirectories() {
		return gradlePlugin(project).getPluginSourceSet().getAllJava().getSourceDirectories();
	}

	public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
		return (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
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
