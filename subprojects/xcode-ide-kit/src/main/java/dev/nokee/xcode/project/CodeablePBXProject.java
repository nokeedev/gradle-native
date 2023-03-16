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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.List;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

@EqualsAndHashCode
public final class CodeablePBXProject implements PBXProject, Codeable {
	public enum CodingKeys implements CodingKey {
		mainGroup,
		targets,
		buildConfigurationList,
		compatibilityVersion,
		projectReferences,
		packageReferences,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	private final KeyedObject delegate;

	public CodeablePBXProject(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public PBXGroup getMainGroup() {
		return delegate.tryDecode(CodingKeys.mainGroup);
	}

	@Override
	public List<PBXTarget> getTargets() {
		return orEmptyList(delegate.tryDecode(CodingKeys.targets));
	}

	@Override
	public XCConfigurationList getBuildConfigurationList() {
		return delegate.tryDecode(CodingKeys.buildConfigurationList);
	}

	@Override
	public String getCompatibilityVersion() {
		return delegate.tryDecode(CodingKeys.compatibilityVersion);
	}

	@Override
	public List<ProjectReference> getProjectReferences() {
		return orEmptyList(delegate.tryDecode(CodingKeys.projectReferences));
	}

	@Override
	public List<XCRemoteSwiftPackageReference> getPackageReferences() {
		return orEmptyList(delegate.tryDecode(CodingKeys.packageReferences));
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public long age() {
		return delegate.age();
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXProject newInstance(KeyedObject delegate) {
		return new CodeablePBXProject(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
