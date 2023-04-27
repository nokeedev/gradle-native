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

import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;

import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXLegacyTarget extends AbstractCodeable implements PBXLegacyTarget {
	public enum CodingKeys implements CodingKey {
		buildArgumentsString,
		buildToolPath,
		buildWorkingDirectory,
		passBuildSettingsInEnvironment,
		name,
		buildPhases,
		buildConfigurationList,
		dependencies,
		productName,
		productType,
		productReference,
		;

		public String getName() {
			return name();
		}
	}

	public CodeablePBXLegacyTarget(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public String getBuildArgumentsString() {
		return tryDecode(CodingKeys.buildArgumentsString);
	}

	@Override
	public String getBuildToolPath() {
		return tryDecode(CodingKeys.buildToolPath);
	}

	@Override
	public String getBuildWorkingDirectory() {
		return tryDecode(CodingKeys.buildWorkingDirectory);
	}

	@Override
	public boolean isPassBuildSettingsInEnvironment() {
		return getOrFalse(CodingKeys.passBuildSettingsInEnvironment);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public String getName() {
		return tryDecode(CodingKeys.name);
	}

	@Override
	public Optional<ProductType> getProductType() {
		return getAsOptional(CodingKeys.productType);
	}

	@Override
	public List<PBXBuildPhase> getBuildPhases() {
		return getOrEmptyList(CodingKeys.buildPhases);
	}

	@Override
	public XCConfigurationList getBuildConfigurationList() {
		return tryDecode(CodingKeys.buildConfigurationList);
	}

	@Override
	public List<PBXTargetDependency> getDependencies() {
		return getOrEmptyList(CodingKeys.dependencies);
	}

	@Override
	public Optional<String> getProductName() {
		return getAsOptional(CodingKeys.productName);
	}

	@Override
	public Optional<PBXFileReference> getProductReference() {
		return getAsOptional(CodingKeys.productReference);
	}

	@Override
	public int stableHash() {
		return getName().hashCode();
	}

	public static CodeablePBXLegacyTarget newInstance(KeyedObject delegate) {
		return new CodeablePBXLegacyTarget(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
