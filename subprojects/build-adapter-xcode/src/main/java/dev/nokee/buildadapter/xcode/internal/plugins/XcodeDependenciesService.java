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
import dev.nokee.xcode.XCProject;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTarget;
import dev.nokee.xcode.XCTargetReference;
import lombok.val;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.util.Path;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class XcodeDependenciesService implements BuildService<XcodeDependenciesService.Parameters> {
	interface Parameters extends BuildServiceParameters {
		MapProperty<XCProjectReference, String> getProjectReferences();
	}

	private final Map<XCTargetReference, Coordinate> targetToCoordinates = new HashMap<>();
	private final Map<XCFileReference, Coordinate> fileToCoordinates = new HashMap<>();

	@Inject
	public XcodeDependenciesService() {
		getParameters().getProjectReferences().get().forEach((reference, path) -> {
			for (XCTarget target : reference.load().getTargets()) {
				val outputFile = target.getOutputFile();
				if (outputFile != null) {
					fileToCoordinates.put(outputFile, new Coordinate(Path.path(path), target.getProject().getName(), target.getName()));
				}
				targetToCoordinates.put(target.asReference(), new Coordinate(Path.path(path), target.getProject().getName(), target.getName()));
			}
		});
	}

	public XCProject load(XCProjectReference reference) {
		return reference.load();
	}

	public XCTarget load(XCTargetReference reference) {
		return reference.load();
	}

	@Nullable
	public Coordinate forFile(XCFileReference file) {
		if (file.getType() == XCFileReference.XCFileType.BUILT_PRODUCT) {
			return Optional.ofNullable(fileToCoordinates.get(file)).orElseThrow(() -> {
				return new RuntimeException("UNKNOWN " + file + "\n" + fileToCoordinates.keySet().stream().map(it -> " - " + it).collect(Collectors.joining("\n")));
			});
		} else {
			return null;
		}
	}

	@Nullable
	public Coordinate forTarget(XCTargetReference target) {
		return Optional.ofNullable(targetToCoordinates.get(target)).orElseThrow(RuntimeException::new);
	}


	public static final class Coordinate {
		public final Path projectPath;
		public final String projectName;
		public final String capabilityName;

		public Coordinate(Path projectPath, String projectName, String capabilityName) {
			this.projectPath = projectPath;
			this.projectName = projectName;
			this.capabilityName = capabilityName;
		}
	}
}
