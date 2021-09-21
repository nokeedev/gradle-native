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
package dev.nokee.ide.xcode;

import dev.nokee.ide.base.IdeWorkspace;
import org.gradle.api.Describable;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

/**
 * Represents the generated Xcode IDE workspace.
 *
 * <blockquote>
 *     <p>
 *         A workspace is an Xcode document that groups projects and other documents so you can work on them together.
 *         A workspace can contain any number of Xcode projects, plus any other files you want to include.
 *         In addition to organizing all the files in each Xcode project, a workspace provides implicit and explicit relationships among the included projects and their targets.
 *     </p>
 * 	   <span>—Xcode Workspace Concept</span>
 * </blockquote>
 *
 * @since 0.3
 * @see <a href="https://developer.apple.com/library/content/featuredarticles/XcodeConcepts/Concept-Workspace.html">Xcode Workspace Concept</a>
 */
public interface XcodeIdeWorkspace extends IdeWorkspace<XcodeIdeProjectReference> {
	/**
	 * Returns Xcode projects to include in the workspace.
	 *
	 * @return a property to configure the projects to include in the workspace.
	 */
	SetProperty<XcodeIdeProjectReference> getProjects();

	/**
	 * Returns the location of the generated workspace.
	 * It defaults to <pre>${project.projectDir}/${project.name}.xcworkspace</pre>.
	 *
	 * @return a provider to the location of the generated workspace.
	 */
	Provider<FileSystemLocation> getLocation();
}
