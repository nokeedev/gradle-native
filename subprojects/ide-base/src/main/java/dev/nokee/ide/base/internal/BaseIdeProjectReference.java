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
package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.IdeProjectReference;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collections;
import java.util.Set;

public class BaseIdeProjectReference implements IdeProjectMetadata, IdeProjectReference, TaskDependency {
	private final Provider<? extends IdeProjectInternal> ideProject;

	public BaseIdeProjectReference(Provider<? extends IdeProjectInternal> ideProject) {
		this.ideProject = ideProject;
	}

	@Override
	public DisplayName getDisplayName() {
		return Describables.withTypeAndName(ideProject.get().getDisplayName(), ideProject.get().getName());
	}

	@Override
	public Set<? extends Task> getGeneratorTasks() {
		return Collections.singleton(ideProject.get().getGeneratorTask().get());
	}

	public Provider<FileSystemLocation> getLocation() {
		return ideProject.flatMap(IdeProject::getLocation);
	}

	/**
	 * @return the effective project location as a {@link File} instance, never null.
	 * @deprecated use {@link #getLocation()} instead.
	 */
	@Deprecated
	@Override
	public File getFile() {
		return ideProject.get().getLocation().get().getAsFile();
	}

	@Override
	public Set<? extends Task> getDependencies(@Nullable Task task) {
		return getGeneratorTasks();
	}
}
