/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTarget;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class XcodeImplicitDependenciesService implements BuildService<XcodeImplicitDependenciesService.Parameters> {
	private static final Logger LOGGER = Logging.getLogger(XcodeImplicitDependenciesService.class);

	interface Parameters extends BuildServiceParameters {
		Property<XCWorkspaceReference> getLocation();
	}

	private final List<XCTarget> targets;
	private final Map<XCProjectReference, String> projectPaths;

	public XcodeImplicitDependenciesService() {
		val workspace = getParameters().getLocation().get().load();
		targets = workspace.getProjectLocations().stream().map(XCProjectReference::load).flatMap(it -> it.getTargets().stream()).map(XCTargetReference::load).collect(Collectors.toList());
		projectPaths = workspace.getProjectLocations().stream().collect(Collectors.toMap(Function.identity(), project -> {
			// TODO: What happen if a workspace reference project in parent directory? It would break the project mapping.
			val relativePath = workspace.getLocation().getParent().relativize(project.getLocation());
			val projectPath = asProjectPath(relativePath);
			LOGGER.info(String.format("Mapping Xcode project '%s' to Gradle project '%s'.", relativePath, projectPath));
			return projectPath;
		}));
	}

	public String asProjectPath(XCProjectReference project) {
		return projectPaths.get(project);
	}

	private static String asProjectPath(Path relativePath) {
		return FilenameUtils.separatorsToUnix(FilenameUtils.removeExtension(relativePath.toString())).replace('/', ':');
	}

	@Nullable
	public XCTarget findTarget(XCFileReference file) {
		if (file.getType() == XCFileReference.XCFileType.BUILT_PRODUCT) {
			return targets.stream().filter(it -> it.getOutputFile().equals(file)).findFirst().orElseThrow(RuntimeException::new);
		} else {
			return null;
		}
	}
}
