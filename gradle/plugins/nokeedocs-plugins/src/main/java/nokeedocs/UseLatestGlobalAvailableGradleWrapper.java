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

import dev.gradleplugins.dockit.samples.Sample;
import dev.gradleplugins.dockit.samples.SampleArchiveArtifact;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollectionSchema;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.wrapper.Wrapper;

final class UseLatestGlobalAvailableGradleWrapper implements Action<Sample> {
	private final Project project;

	public UseLatestGlobalAvailableGradleWrapper(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Sample sample) {
		final TaskProvider<Wrapper> gradleWrapperTask = registerGradleWrapperTaskIfAbsent(project);
		sample.getArtifacts().withType(SampleArchiveArtifact.class).configureEach(artifact -> artifact.content(spec -> spec.from(gradleWrapperTask.map(it -> it.getExtensions().getByName("destinationDirectory")))));
	}

	private static TaskProvider<Wrapper> registerGradleWrapperTaskIfAbsent(Project project) {
		for (NamedDomainObjectCollectionSchema.NamedDomainObjectSchema element : project.getTasks().getCollectionSchema().getElements()) {
			if (element.getName().equals("generateLatestGlobalAvailableGradleWrapper")) {
				return project.getTasks().named("generateLatestGlobalAvailableGradleWrapper", Wrapper.class);
			}
		}

		return project.getTasks().register("generateLatestGlobalAvailableGradleWrapper", Wrapper.class, task -> {
			Provider<Directory> outputDirectory = project.getLayout().getBuildDirectory().dir("tmp/" + task.getName());
			task.setGradleVersion(getLatestGlobalAvailableVersion());
			task.setScriptFile(outputDirectory.get().file("gradlew"));
			task.setJarFile(outputDirectory.get().file("gradle/wrapper/gradle-wrapper.jar"));
			task.getExtensions().add("destinationDirectory", outputDirectory);
		});
	}

	private static String getLatestGlobalAvailableVersion() {
		return "7.4.1";
	}
}
