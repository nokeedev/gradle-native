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
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build system.
 */
public interface PBXNativeTarget extends PBXTarget {
	List<XCSwiftPackageProductDependency> getPackageProductDependencies();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXNativeTarget>, BuildPhaseAwareBuilder<Builder>, BuildConfigurationsAwareBuilder<Builder> {
		private String name;
		private ProductType productType;
		private final List<PBXBuildPhase> buildPhases = new ArrayList<>();
		private XCConfigurationList buildConfigurationList;
		private String productName;
		private PBXFileReference productReference;
		private final List<PBXTargetDependency> dependencies = new ArrayList<>();
		private final List<XCSwiftPackageProductDependency> packageProductDependencies = new ArrayList<>();

		public Builder name(String name) {
			this.name = requireNonNull(name);
			return this;
		}

		public Builder productType(ProductType productType) {
			this.productType = requireNonNull(productType);
			return this;
		}

		@Override
		public Builder buildPhase(PBXBuildPhase buildPhase) {
			this.buildPhases.add(requireNonNull(buildPhase));
			return this;
		}

		@Override
		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			this.buildPhases.clear();
			stream(buildPhases).map(Objects::requireNonNull).forEach(this.buildPhases::add);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurationList) {
			this.buildConfigurationList = requireNonNull(buildConfigurationList);
			return this;
		}

		public Builder productName(String productName) {
			this.productName = requireNonNull(productName);
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			this.productReference = requireNonNull(productReference);
			return this;
		}

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			this.dependencies.clear();
			stream(dependencies).map(Objects::requireNonNull).forEach(this.dependencies::add);
			return this;
		}

		public Builder packageProductDependencies(Iterable<? extends XCSwiftPackageProductDependency> packageProductDependencies) {
			this.packageProductDependencies.clear();
			stream(packageProductDependencies).map(Objects::requireNonNull).forEach(this.packageProductDependencies::add);
			return this;
		}

		@Override
		public PBXNativeTarget build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXNativeTarget");
			builder.put(CodeablePBXNativeTarget.CodingKeys.name, requireNonNull(name, "'name' must not be null"));
			builder.put(CodeablePBXNativeTarget.CodingKeys.productType, productType);
			builder.put(CodeablePBXNativeTarget.CodingKeys.buildPhases, ImmutableList.copyOf(buildPhases));
			builder.put(CodeablePBXNativeTarget.CodingKeys.productName, requireNonNull(productName, "'productName' must not be null"));
			builder.put(CodeablePBXNativeTarget.CodingKeys.productReference, requireNonNull(productReference, "'productReference' must not be null"));
			builder.put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, requireNonNull(buildConfigurationList, "'buildConfigurations' must not be null"));
			builder.put(CodeablePBXNativeTarget.CodingKeys.dependencies, ImmutableList.copyOf(dependencies));
			builder.put(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, ImmutableList.copyOf(packageProductDependencies));

			return new CodeablePBXNativeTarget(builder.build());
		}
	}
}
