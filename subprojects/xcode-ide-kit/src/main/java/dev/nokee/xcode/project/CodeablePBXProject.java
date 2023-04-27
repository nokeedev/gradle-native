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
package dev.nokee.xcode.project;

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.PBXTarget;

import java.util.List;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXProject extends AbstractCodeable implements PBXProject {
	public enum CodingKeys implements CodingKey {
		mainGroup,
		targets,
		buildConfigurationList,
		compatibilityVersion,
		projectReferences,
		packageReferences,
		projectDirPath,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXProject(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public PBXGroup getMainGroup() {
		return getOrNull(CodingKeys.mainGroup);
	}

	@Override
	public List<PBXTarget> getTargets() {
		return getOrEmptyList(CodingKeys.targets);
	}

	@Override
	public XCConfigurationList getBuildConfigurationList() {
		return getOrNull(CodingKeys.buildConfigurationList);
	}

	@Override
	public String getCompatibilityVersion() {
		return getOrNull(CodingKeys.compatibilityVersion);
	}

	@Override
	public List<ProjectReference> getProjectReferences() {
		return getOrEmptyList(CodingKeys.projectReferences);
	}

	@Override
	public List<XCRemoteSwiftPackageReference> getPackageReferences() {
		return getOrEmptyList(CodingKeys.packageReferences);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static CodeablePBXProject newInstance(KeyedObject delegate) {
		return new CodeablePBXProject(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
