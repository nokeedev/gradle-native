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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.javadoc.Javadoc;

import javax.inject.Inject;
import java.util.Arrays;

import static dev.gradleplugins.dockit.javadoc.JavadocLinksOption.links;
import static dev.gradleplugins.dockit.javadoc.JavadocTitleOption.title;
import static net.nokeedev.jbake.JBakeExtension.jbake;

abstract class NokeeJavadocPlugin implements Plugin<Project> {
	@Inject
	public NokeeJavadocPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.javadoc-api-reference");

		project.getTasks().named("apiReferenceJavadoc", Javadoc.class, task -> {
			title(task).set("Nokee v" + project.getVersion());
			links(task).addAll(project.getProviders().gradleProperty("minimumGradleVersion").map(version -> {
				return Arrays.asList(
//					project.uri("https://docs.oracle.com/javase/" + minimumJavaVersionFor(version).getMajorVersion() + "/docs/api"),
					project.uri("https://docs.gradle.org/" + version + "/javadoc/")
				);
			}));
		});

		project.getPluginManager().withPlugin("net.nokeedev.jbake-site", ignored -> {
			jbake(project, extension -> {
				extension.getAssets().from(extension.sync("javadoc", spec -> {
					spec.into("javadoc", it -> it.from(project.getTasks().named("apiReferenceJavadoc", Javadoc.class).map(Javadoc::getDestinationDir)));
				}));
			});
		});
	}
}
