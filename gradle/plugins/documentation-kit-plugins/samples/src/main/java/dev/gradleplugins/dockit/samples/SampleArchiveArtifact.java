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
package dev.gradleplugins.dockit.samples;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;

import javax.inject.Inject;

public abstract class SampleArchiveArtifact extends SampleArtifact {
	private final TaskProvider<Sync> stageTask;
	private final TaskProvider<Zip> zipTask;
	private final Provider<Directory> stageDirectory;

	@Inject
	public SampleArchiveArtifact(String name, Project project) {
		super(name);

		this.stageTask = project.getTasks().register("stage" + capitalized(name), Sync.class, task -> {
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName()).get().getAsFile());
			task.setIncludeEmptyDirs(false);
			task.exclude(".gradle");
		});
		this.stageDirectory = project.getObjects().directoryProperty().fileProvider(stageTask.map(Sync::getDestinationDir));
		this.zipTask = project.getTasks().register("zip" + capitalized(name), Zip.class, task -> {
			task.from(stageTask);
			task.getDestinationDirectory().value(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())).disallowChanges();
			task.getArchiveBaseName().value(getBaseName()).disallowChanges();
			task.getArchiveClassifier().value(getDsl().map(Dsl::getName)).disallowChanges();
			task.getArchiveVersion().value(getProductVersion()).disallowChanges();
		});
	}

	public abstract Property<String> getBaseName();

	public abstract Property<Dsl> getDsl();

	public abstract Property<String> getProductVersion();

	public void content(Action<? super CopySpec> action) {
		stageTask.configure(action);
	}

	public Provider<RegularFile> getArchiveFile() {
		return zipTask.flatMap(Zip::getArchiveFile);
	}

	public Provider<Directory> getStageDirectory() {
		return stageDirectory;
	}

	private static String capitalized(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
