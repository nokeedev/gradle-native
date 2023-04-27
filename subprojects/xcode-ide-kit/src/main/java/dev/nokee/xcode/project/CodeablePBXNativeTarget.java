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
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;

import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXNativeTarget extends AbstractCodeable implements PBXNativeTarget {
	public enum CodingKeys implements CodingKey {
		packageProductDependencies,
		name,
		productType,
		buildPhases,
		buildConfigurationList,
		dependencies,
		productName,
		productReference,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXNativeTarget(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public List<XCSwiftPackageProductDependency> getPackageProductDependencies() {
		return getOrEmptyList(CodingKeys.packageProductDependencies);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public String getName() {
		return getOrNull(CodingKeys.name);
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
		return getOrNull(CodingKeys.buildConfigurationList);
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
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public int stableHash() {
		return getName().hashCode();
	}

	public static CodeablePBXNativeTarget newInstance(KeyedObject delegate) {
		return new CodeablePBXNativeTarget(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
