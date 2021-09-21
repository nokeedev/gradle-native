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
package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeWorkspaceInternal;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import dev.nokee.ide.visualstudio.VisualStudioIdeSolution;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeSolutionTask;
import lombok.Getter;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeSolution implements VisualStudioIdeSolution, IdeWorkspaceInternal<VisualStudioIdeProjectReference> {
	@Getter private final TaskProvider<GenerateVisualStudioIdeSolutionTask> generatorTask;

	@Inject
	public DefaultVisualStudioIdeSolution() {
		generatorTask = getTasks().register("visualStudioSolution", GenerateVisualStudioIdeSolutionTask.class);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Override
	public Provider<FileSystemLocation> getLocation() {
		return generatorTask.flatMap(GenerateVisualStudioIdeSolutionTask::getSolutionLocation);
	}

	@Override
	public String getDisplayName() {
		return "Visual Studio solution";
	}
}
