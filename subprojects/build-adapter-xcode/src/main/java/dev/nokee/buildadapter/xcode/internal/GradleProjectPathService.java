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
package dev.nokee.buildadapter.xcode.internal;

import dev.nokee.buildadapter.xcode.internal.rules.XcodeBuildLayoutRule;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.nio.file.Path;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

public final class GradleProjectPathService {
	private static final Logger LOGGER = Logging.getLogger(GradleProjectPathService.class);

	private final Path baseDirectory;

	public GradleProjectPathService(Path baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String toProjectPath(XCProjectReference reference) {
		val relativePath = baseDirectory.relativize(reference.getLocation());
		val projectPath = ":" + asProjectPath(relativePath);
		LOGGER.info(String.format("Mapping Xcode project '%s' to Gradle project '%s'.", relativePath, projectPath));
		return projectPath;
	}

	private static String asProjectPath(Path relativePath) {
		return separatorsToUnix(FilenameUtils.removeExtension(relativePath.toString())).replace('/', ':');
	}
}
