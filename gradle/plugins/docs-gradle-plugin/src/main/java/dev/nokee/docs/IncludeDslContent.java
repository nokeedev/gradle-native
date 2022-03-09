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

import dev.gradleplugins.dockit.samples.Dsl;
import dev.gradleplugins.dockit.samples.Sample;
import dev.gradleplugins.dockit.samples.SampleArchiveArtifact;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class IncludeDslContent implements Action<Sample> {
	private final Project project;

	public IncludeDslContent(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Sample sample) {
		final Property<PluginManagementBlock> pluginManagementBlockProperty = project.getObjects().property(PluginManagementBlock.class);
		pluginManagementBlockProperty.convention(project.provider(() -> PluginManagementBlock.nokee(project.getVersion().toString())));
		sample.getExtensions().add("pluginManagementBlock", pluginManagementBlockProperty);
		sample.getArtifacts().withType(SampleArchiveArtifact.class).configureEach(archive -> {
			archive.content(rootSpec -> {
				rootSpec.from(sample.getSampleDirectory().dir(archive.getDsl().map(Dsl::getName)),
					spec -> spec.exclude(archive.getDsl().get().getSettingsFileName()));
				rootSpec.from(includePluginManagementBlock(project, sample, archive, pluginManagementBlockProperty));
			});
		});
	}

	private static Callable<Object> includePluginManagementBlock(Project project, Sample sample, SampleArchiveArtifact archive, Property<PluginManagementBlock> pluginManagementBlockProperty) {
		return new Callable<Object>() {
			private TaskProvider<Task> task = null;

			@Override
			public Object call() throws Exception {
				if (task == null) {
					task = project.getTasks().register(archive.getName(), task -> {
						final Provider<RegularFile> originalSettingsFile = sample.getSampleDirectory().file(archive.getDsl().map(it -> it.getName() + "/" + it.getSettingsFileName()));
						task.getInputs().file(originalSettingsFile);

						final Provider<String> pluginManagementBlock = pluginManagementBlockProperty.map(it -> {
							if (archive.getDsl().get().equals(Dsl.GROOVY_DSL)) {
								return it.asGroovyDsl().toString();
							} else if (archive.getDsl().get().equals(Dsl.KOTLIN_DSL)) {
								return it.asKotlinDsl().toString();
							} else {
								throw new UnsupportedOperationException();
							}
						});
						task.getInputs().property("content", pluginManagementBlock);

						final Provider<RegularFile> newSettingsFile = project.getLayout().getBuildDirectory().file("tmp/" + task.getName() + "/" + archive.getDsl().get().getSettingsFileName());
						task.getOutputs().file(newSettingsFile);
						task.doLast(new Action<Task>() {
							@Override
							public void execute(Task task) {
								try {
									List<String> lines = Stream.concat(
										Arrays.stream(pluginManagementBlock.get().split("\r?\n")),
										Files.lines(originalSettingsFile.get().getAsFile().toPath())
									).collect(Collectors.toList());
									Files.write(newSettingsFile.get().getAsFile().toPath(), lines);
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							}
						});
					});
				}
				return task;
			}
		};
	}
}
