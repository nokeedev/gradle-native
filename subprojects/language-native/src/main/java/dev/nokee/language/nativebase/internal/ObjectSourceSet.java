/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.language.nativebase.internal;

import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public final class ObjectSourceSet {
	private final String name;
	private final TaskProvider<? extends Task> generatedByTask;
	private final ConfigurableFileTree fileTree;

	public ObjectSourceSet(String name, Provider<Directory> sourceDirectory, TaskProvider<? extends Task> generatedByTask, ObjectFactory objectFactory) {
		this.name = name;
		this.generatedByTask = generatedByTask;
		this.fileTree = objectFactory.fileTree();
		this.fileTree.setDir(sourceDirectory).builtBy(generatedByTask).include("**/*.o", "**/*.obj");
	}

	public TaskProvider<? extends Task> getGeneratedByTask() {
		return generatedByTask;
	}

	public String getName() {
		return name;
	}

	public FileTree getAsFileTree() {
		return fileTree;
	}
}
