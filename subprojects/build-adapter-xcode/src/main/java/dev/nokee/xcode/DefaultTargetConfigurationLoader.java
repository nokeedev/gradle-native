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
package dev.nokee.xcode;

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.io.Serializable;

@EqualsAndHashCode
public final class DefaultTargetConfigurationLoader implements XCLoader<String, XCTargetReference>, Serializable {
	private final XCLoader<PBXProject, XCProjectReference> loader;

	public DefaultTargetConfigurationLoader(XCLoader<PBXProject, XCProjectReference> loader) {
		this.loader = loader;
	}

	@Override
	public String load(XCTargetReference reference) {
		final PBXProject project = loader.load(reference.getProject());
		final PBXTarget target = findTarget(project, reference.getName());
		if (target == null) {
			throw new RuntimeException(String.format("%s refers to unknown target", reference));
		}

		String result = defaultBuildConfigurationName(target);
		if (result == null) {
			result = defaultBuildConfigurationName(project);
		}

		return result;
	}

	@Nullable
	private static PBXTarget findTarget(PBXProject project, String targetName) {
		assert project != null : "'project' must not be null";
		assert targetName != null : "'targetName' must not be null";

		for (PBXTarget target : project.getTargets()) {
			if (targetName.equals(target.getName())) {
				return target;
			}
		}
		return null; // no target found
	}

	@Nullable
	private static String defaultBuildConfigurationName(PBXTarget target) {
		assert target != null : "'target' must not be null";
		return defaultBuildConfigurationName(target.getBuildConfigurationList());
	}

	@Nullable
	private static String defaultBuildConfigurationName(PBXProject project) {
		assert project != null : "'project' must not be null";
		return defaultBuildConfigurationName(project.getBuildConfigurationList());
	}

	@Nullable
	private static String defaultBuildConfigurationName(@Nullable XCConfigurationList buildConfigurations) {
		if (buildConfigurations == null) {
			return null;
		} else {
			return buildConfigurations.getDefaultConfigurationName().orElse(null);
		}
	}
}
