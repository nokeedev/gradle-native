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
package dev.nokee.xcode.objects.targets;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Information for building a specific artifact (a library, binary, or test).
 */
public abstract class PBXTarget extends PBXProjectItem {
	private final String name;
	@Nullable private final ProductType productType;
	private final ImmutableList<PBXBuildPhase> buildPhases;
	private final XCConfigurationList buildConfigurationList;
	@Nullable private final String productName;
	@Nullable private final PBXFileReference productReference;
	private final ImmutableList<PBXTargetDependency> dependencies;

	protected PBXTarget(String name, @Nullable ProductType productType, ImmutableList<PBXBuildPhase> buildPhases, XCConfigurationList buildConfigurationList, @Nullable String productName, @Nullable PBXFileReference productReference, ImmutableList<PBXTargetDependency> dependencies) {
		this.name = name;
		this.productType = productType;
		this.buildPhases = buildPhases;
		this.buildConfigurationList = buildConfigurationList;
		this.productName = productName;
		this.productReference = productReference;
		this.dependencies = dependencies;
	}

	public String getName() {
		return name;
	}

	public Optional<ProductType> getProductType() {
		return Optional.ofNullable(productType);
	}

	public List<PBXBuildPhase> getBuildPhases() {
		return buildPhases;
	}

	public XCConfigurationList getBuildConfigurationList() {
		return buildConfigurationList;
	}

	public List<PBXTargetDependency> getDependencies() {
		return dependencies;
	}

	public Optional<String> getProductName() {
		return Optional.ofNullable(productName);
	}

	public Optional<PBXFileReference> getProductReference() {
		return Optional.ofNullable(productReference);
	}
}
