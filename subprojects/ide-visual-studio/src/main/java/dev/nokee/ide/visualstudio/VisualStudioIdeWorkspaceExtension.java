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

/**
 * The configuration for mapping a Gradle project to Visual Studio projects and solution.
 *
 * The workspace extension is register to the {@link org.gradle.api.Project} instance only for the root project where the Visual Studio IDE plugin is applied.
 * The project extension, that is {@link VisualStudioIdeProjectExtension}, is register to sub-projects instead.
 *
 * @since 0.5
 */
public interface VisualStudioIdeWorkspaceExtension extends VisualStudioIdeProjectExtension {
	/**
	 * Returns the generated Visual Studio solution for this Gradle build.
	 *
	 * @return a {@link VisualStudioIdeSolution} instance, never null.
	 */
	VisualStudioIdeSolution getSolution();
}
