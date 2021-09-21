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

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;

/**
 * The configuration for mapping a Gradle project to Xcode projects.
 *
 * The project extension is register to sub-projects where the Xcode IDE plugin is applied.
 * The workspace extension, that is {@link XcodeIdeWorkspaceExtension}, is register to the root project instead.
 *
 * @since 0.3
 */
public interface XcodeIdeProjectExtension {
	/**
	 * Returns the projects to generate for this Gradle project.
	 *
	 * @return a container of {@link XcodeIdeProject} to configure the projects to create, never null.
	 */
	NamedDomainObjectContainer<XcodeIdeProject> getProjects();

	/**
	 * Configures the project container with the specified action.
	 *
	 * @param action a configuration action for the container of {@link XcodeIdeProject} instances.
	 * @throws NullPointerException if the action is null
	 */
	void projects(Action<? super NamedDomainObjectContainer<XcodeIdeProject>> action);

	/**
	 * Returns a repository containing all the known product types by the plugin.
	 * This method is provided as a convenience when configuring the Xcode project via the DSL.
	 *
	 * It is equivalent to importing the {@link XcodeIdeProductTypes} class and accessing the public field:
	 * <pre class="autoTested">
	 * plugins {
	 *     id 'dev.nokee.xcode-ide'
	 * }
	 *
	 * import dev.nokee.ide.xcode.XcodeIdeProductTypes
	 *
	 * xcode {
	 *     projects.register('foo') {
	 *         targets.register('Foo') {
	 *             productType = XcodeIdeProductTypes.APPLICATION
	 *         }
	 *     }
	 * }
	 * </pre>
	 *
	 * @return a {@link XcodeIdeProductTypes} instance to access all known product types via the DSL, never, null.
	 */
	XcodeIdeProductTypes getProductTypes();
}
