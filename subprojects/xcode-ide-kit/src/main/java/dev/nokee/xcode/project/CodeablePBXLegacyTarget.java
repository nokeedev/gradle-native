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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;
import static dev.nokee.xcode.project.PBXTypeSafety.orFalse;

@EqualsAndHashCode
public final class CodeablePBXLegacyTarget implements PBXLegacyTarget, Codeable {
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

	private final KeyedObject delegate;

	public CodeablePBXLegacyTarget(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getBuildArgumentsString() {
		return delegate.tryDecode(CodingKeys.buildArgumentsString);
	}

	@Override
	public String getBuildToolPath() {
		return delegate.tryDecode(CodingKeys.buildToolPath);
	}

	@Override
	public String getBuildWorkingDirectory() {
		return delegate.tryDecode(CodingKeys.buildWorkingDirectory);
	}

	@Override
	public boolean isPassBuildSettingsInEnvironment() {
		return orFalse(delegate.tryDecode(CodingKeys.passBuildSettingsInEnvironment));
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public String getName() {
		return delegate.tryDecode(CodingKeys.name);
	}

	@Override
	public Optional<ProductType> getProductType() {
		return orEmpty(delegate.tryDecode(CodingKeys.productType));
	}

	@Override
	public List<PBXBuildPhase> getBuildPhases() {
		return orEmptyList(delegate.tryDecode(CodingKeys.buildPhases));
	}

	@Override
	public XCConfigurationList getBuildConfigurationList() {
		return delegate.tryDecode(CodingKeys.buildConfigurationList);
	}

	@Override
	public List<PBXTargetDependency> getDependencies() {
		return orEmptyList(delegate.tryDecode(CodingKeys.dependencies));
	}

	@Override
	public Optional<String> getProductName() {
		return orEmpty(delegate.tryDecode(CodingKeys.productName));
	}

	@Override
	public Optional<PBXFileReference> getProductReference() {
		return orEmpty(delegate.tryDecode(CodingKeys.productReference));
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
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public int stableHash() {
		return getName().hashCode();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}
}
