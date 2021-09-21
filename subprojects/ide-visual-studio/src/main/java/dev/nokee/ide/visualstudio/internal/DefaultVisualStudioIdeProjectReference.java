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

import dev.nokee.ide.base.internal.BaseIdeProjectReference;
import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.util.Set;

public final class DefaultVisualStudioIdeProjectReference extends BaseIdeProjectReference implements VisualStudioIdeProjectReference {
	private final Provider<DefaultVisualStudioIdeProject> ideProject;

	public DefaultVisualStudioIdeProjectReference(Provider<DefaultVisualStudioIdeProject> ideProject) {
		super(ideProject);
		this.ideProject = ideProject;
	}

	@Override
	public Provider<FileSystemLocation> getProjectLocation() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectLocation);
	}

	@Override
	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectGuid);
	}

	@Override
	public Provider<Set<VisualStudioIdeProjectConfiguration>> getProjectConfigurations() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectConfigurations);
	}
}
