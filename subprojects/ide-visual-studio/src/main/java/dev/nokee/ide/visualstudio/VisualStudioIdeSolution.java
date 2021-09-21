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
package dev.nokee.ide.visualstudio;

import dev.nokee.ide.base.IdeWorkspace;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

/**
 * Represents the generated Visual Studio IDE solution.
 *
 * @since 0.5
 */
public interface VisualStudioIdeSolution extends IdeWorkspace<VisualStudioIdeProjectReference> {
	/**
	 * Returns Visual Studio projects to include in the solution.
	 *
	 * @return a property to configure the projects to include in the solution.
	 */
	SetProperty<VisualStudioIdeProjectReference> getProjects();

	/**
	 * Returns the location of the generated solution.
	 * It defaults to <pre>${project.projectDir}/${project.name}.sln</pre>.
	 *
	 * @return a provider to the location of the generated workspace.
	 */
	Provider<FileSystemLocation> getLocation();
}
