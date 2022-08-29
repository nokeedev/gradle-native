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
package nokeedocs.templates;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class SampleTemplatesPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("java-base");

		sourceSets(project, registerTemplatesSourceSet(project));
	}

	private static Action<SourceSetContainer> registerTemplatesSourceSet(Project project) {
		return sourceSets -> {
			sourceSets.register("templates", sourceSet -> {
				// So the SourceSet#getApiConfigurationName() exists
				java(project, t -> t.registerFeature("templates", it -> {
					it.capability("dev.nokee", project.getName() + "-templates", "1.0");
					it.usingSourceSet(sourceSet);
				}));

				final File[] samples = requireNonNull(project.file("src/" + sourceSet.getName()).listFiles(File::isDirectory));

				final String generateSourcesTaskName = sourceSet.getTaskName("generate", "sources");
				sourceSet.getExtensions().add(String.class, "generateSourcesTaskName", generateSourcesTaskName);
				final TaskProvider<GenerateSources> generatorTask = project.getTasks().register(generateSourcesTaskName, GenerateSources.class, task -> {
					task.getDestinationDirectory().set(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()));
					task.getPackageName().set(project.getGroup().toString());
					task.getSamples().set(Arrays.stream(samples).map(File::getName).collect(Collectors.toList()));
				});
				sourceSet.getJava().srcDir(generatorTask.flatMap(GenerateSources::getDestinationDirectory));

				project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, task -> {
					task.source(generatorTask.flatMap(GenerateSources::getDestinationDirectory));
				});

				project.getDependencies().add(sourceSet.getApiConfigurationName(), "dev.gradleplugins:gradle-fixtures-source-elements:latest.release");


				final String processSamplesTaskName = sourceSet.getTaskName("process", "samples");
				sourceSet.getExtensions().add(String.class, "processSamplesTaskName", processSamplesTaskName);
				final TaskProvider<Sync> processorTask = project.getTasks().register(processSamplesTaskName, Sync.class, task -> {
					Map<String, Collection<String>> receipt = new LinkedHashMap<>();
					Arrays.stream(samples).forEach( sampleDir -> {
						task.from(sampleDir, spec -> spec.into(sampleDir.getName()));
					});

					task.eachFile(details -> {
						// Compute default value
						receipt.computeIfAbsent(details.getRelativePath().getSegments()[0], key -> {
							if (Arrays.stream(samples).anyMatch(it -> it.getName().equals(key))) {
								return new ArrayList<>();
							} else {
								return null;
							}
						});
						receipt.computeIfPresent(details.getRelativePath().getSegments()[0], (key, value) -> {
							value.add(details.getRelativePath().toString());
							return value;
						});
					});
					task.doLast(new Action<Task>() {
						@Override
						public void execute(Task ignored) {
							receipt.forEach((k, v) -> {
								try {
									Files.write(task.getDestinationDir().toPath().resolve(k + ".sample"), v);
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							});
						}
					});
					task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())));
				});

				project.getTasks().named(sourceSet.getProcessResourcesTaskName(), ProcessResources.class, task -> {
					task.from(processorTask);
				});
			});
		};
	}

	private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
		project.getExtensions().configure("sourceSets", action);
	}

	private static void java(Project project, Action<? super JavaPluginExtension> action) {
		project.getExtensions().configure("java", action);
	}
}
