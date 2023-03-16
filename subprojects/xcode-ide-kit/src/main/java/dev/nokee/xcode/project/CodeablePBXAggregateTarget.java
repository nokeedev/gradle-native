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
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

@EqualsAndHashCode
public final class CodeablePBXAggregateTarget implements PBXAggregateTarget, Codeable {
	public enum CodingKeys implements CodingKey {
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

	private final KeyedObject delegate;

	public CodeablePBXAggregateTarget(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.tryDecode(CodingKeys.name);
	}

	@Override
	public Optional<ProductType> getProductType() {
		return Optional.ofNullable(delegate.tryDecode(CodingKeys.productType));
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
	public Builder toBuilder() {
		return new Builder(delegate);
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
	public String toString() {
		return String.format("%s", delegate);
	}

	@Override
	public int stableHash() {
		return getName().hashCode();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXAggregateTarget newInstance(KeyedObject delegate) {
		return new CodeablePBXAggregateTarget(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
