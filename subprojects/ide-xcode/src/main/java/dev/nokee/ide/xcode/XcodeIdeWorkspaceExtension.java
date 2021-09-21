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

/**
 * The configuration for mapping a Gradle project to Xcode projects and workspace.
 *
 * The workspace extension is register to the {@link org.gradle.api.Project} instance only for the root project where the Xcode IDE plugin is applied.
 * The project extension, that is {@link XcodeIdeProjectExtension}, is register to sub-projects instead.
 *
 * @since 0.3
 */
public interface XcodeIdeWorkspaceExtension extends XcodeIdeProjectExtension {
	/**
	 * Returns the generated Xcode workspace for this Gradle build.
	 *
	 * @return a {@link XcodeIdeWorkspace} instance, never null.
	 */
	XcodeIdeWorkspace getWorkspace();
}
