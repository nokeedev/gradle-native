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
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build system.
 */
public interface PBXNativeTarget extends PBXTarget {
	List<XCSwiftPackageProductDependency> getPackageProductDependencies();

	@Override
	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXNativeTarget>, BuildPhaseAwareBuilder<Builder>, BuildConfigurationsAwareBuilder<Builder>, LenientAwareBuilder<Builder>, TaskDependenciesAwareBuilder<Builder>, PBXTarget.Builder {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder());
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXNativeTarget");
			builder.requires(key(CodeablePBXNativeTarget.CodingKeys.name));
			builder.requires(key(CodeablePBXNativeTarget.CodingKeys.productName));
			builder.requires(key(CodeablePBXNativeTarget.CodingKeys.productReference));
			builder.requires(key(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.name, name);
			return this;
		}

		public Builder productType(ProductType productType) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.productType, productType);
			return this;
		}

		@Override
		public Builder buildPhase(PBXBuildPhase buildPhase) {
			builder.add(CodeablePBXNativeTarget.CodingKeys.buildPhases, buildPhase);
			return this;
		}
		@Override
		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			List<PBXBuildPhase> sanitizedBuildPhases = stream(buildPhases).peek(Objects::requireNonNull).collect(ImmutableList.toImmutableList());
			builder.put(CodeablePBXNativeTarget.CodingKeys.buildPhases, sanitizedBuildPhases);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurationList) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, buildConfigurationList);
			return this;
		}

		public Builder productName(String productName) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.productName, productName);
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.productReference, productReference);
			return this;
		}

		@Override
		public Builder dependency(PBXTargetDependency dependency) {
			builder.add(CodeablePBXNativeTarget.CodingKeys.dependencies, dependency);
			return this;
		}

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.dependencies, dependencies);
			return this;
		}

		public Builder packageProductDependencies(Iterable<? extends XCSwiftPackageProductDependency> packageProductDependencies) {
			builder.put(CodeablePBXNativeTarget.CodingKeys.dependencies, packageProductDependencies);
			return this;
		}

		@Override
		public PBXNativeTarget build() {
			return new CodeablePBXNativeTarget(builder.build());
		}
	}
}
