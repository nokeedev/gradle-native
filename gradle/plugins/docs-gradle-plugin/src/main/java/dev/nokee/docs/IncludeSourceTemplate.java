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
package dev.nokee.docs;

import dev.gradleplugins.dockit.samples.Sample;
import dev.gradleplugins.dockit.samples.SampleArchiveArtifact;
import dev.gradleplugins.fixtures.sources.SourceElement;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.io.UncheckedIOException;

final class IncludeSourceTemplate implements Action<Sample> {
	private final Project project;

	public IncludeSourceTemplate(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Sample sample) {
		final Property<SourceElement> templateProperty = project.getObjects().property(SourceElement.class);
		sample.getExtensions().add("template", templateProperty);
		final TaskProvider<Task> sourceTask = project.getTasks().register(sample.taskName("generate", "source"), task -> {
			final Provider<Directory> destinationDirectory = project.getLayout().getBuildDirectory().dir("tmp/" + task.getName());
			task.getExtensions().add("destinationDirectory", destinationDirectory);
			task.getOutputs().dir(destinationDirectory);
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					try {
						FileUtils.cleanDirectory(destinationDirectory.get().getAsFile());
						templateProperty.get().writeToProject(destinationDirectory.get().getAsFile());
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			});
		});
		sample.getArtifacts().withType(SampleArchiveArtifact.class, archive -> {
			archive.content(spec -> spec.from(sourceTask.map(it -> it.getExtensions().getByName("destinationDirectory"))));
		});
	}
}
