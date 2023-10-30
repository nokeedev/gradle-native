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
package dev.nokee.model.internal;

import com.google.common.collect.Iterators;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;
import org.gradle.api.Project;
import org.gradle.util.Path;

import java.util.Iterator;

@EqualsAndHashCode(doNotUseGetters = true)
public final class ProjectIdentifier implements ModelObjectIdentifier {
	private final Path projectPath;
	private final String projectName;

	private ProjectIdentifier(Path projectPath, String projectName) {
		this.projectPath = projectPath;
		this.projectName = projectName;
	}

	public ElementName getName() {
		return ElementName.of(projectName);
	}

	public Path getPath() {
		return projectPath;
	}

	@Override
	public ModelObjectIdentifier getParent() {
		return null;
	}

	public static ProjectIdentifier of(String name) {
		return new ProjectIdentifier(Path.ROOT.child(name), name);
	}

	public static ProjectIdentifier ofRootProject() {
		return new ProjectIdentifier(Path.ROOT, null);
	}

	public static ProjectIdentifier ofChildProject(String... projectNames) {
		Path path = Path.ROOT;
		for (String projectName : projectNames) {
			path = path.child(projectName);
		}
		return new ProjectIdentifier(path, projectNames[projectNames.length - 1]);
	}

	public static ProjectIdentifier of(Project project) {
		return new ProjectIdentifier(Path.path(project.getPath()), project.getName());
	}

	@Override
	public String toString() {
		return "project '" + projectPath + "'";
	}

	@Override
	public Iterator<Object> iterator() {
		// TODO: Use identity instead of this
		return Iterators.forArray(this);
	}
}
