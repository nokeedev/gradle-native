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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build system.
 */
public final class PBXNativeTarget extends PBXTarget {
	private final ImmutableList<XCSwiftPackageProductDependency> packageProductDependencies;

	private PBXNativeTarget(String name, ProductType productType, ImmutableList<PBXBuildPhase> buildPhases, XCConfigurationList buildConfigurationList, String productName, PBXFileReference productReference, ImmutableList<PBXTargetDependency> dependencies, ImmutableList<XCSwiftPackageProductDependency> packageProductDependencies) {
		super(name, productType, buildPhases, buildConfigurationList, productName, productReference, dependencies);
		this.packageProductDependencies = packageProductDependencies;
	}

	public List<XCSwiftPackageProductDependency> getPackageProductDependencies() {
		return packageProductDependencies;
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private ProductType productType;
		private final List<PBXBuildPhase> buildPhases = new ArrayList<>();
		private XCConfigurationList buildConfigurationList;
		private String productName;
		private PBXFileReference productReference;
		private final List<PBXTargetDependency> dependencies = new ArrayList<>();
		private final List<XCSwiftPackageProductDependency> packageProductDependencies = new ArrayList<>();

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder productType(ProductType productType) {
			this.productType = Objects.requireNonNull(productType);
			return this;
		}

		public Builder buildPhase(PBXBuildPhase buildPhase) {
			this.buildPhases.add(Objects.requireNonNull(buildPhase));
			return this;
		}

		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			this.buildPhases.clear();
			stream(buildPhases).map(Objects::requireNonNull).forEach(this.buildPhases::add);
			return this;
		}

		public Builder buildConfigurations(Consumer<? super XCConfigurationList.Builder> builderConsumer) {
			final XCConfigurationList.Builder builder = XCConfigurationList.builder();
			builderConsumer.accept(builder);
			this.buildConfigurationList = builder.build();
			return this;
		}

		public Builder buildConfigurations(XCConfigurationList buildConfigurationList) {
			this.buildConfigurationList = Objects.requireNonNull(buildConfigurationList);
			return this;
		}

		public Builder productName(String productName) {
			this.productName = Objects.requireNonNull(productName);
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			this.productReference = Objects.requireNonNull(productReference);
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

		public PBXNativeTarget build() {
			return new PBXNativeTarget(Objects.requireNonNull(name, "'name' must not be null"), Objects.requireNonNull(productType, "'productType' must not be null"), ImmutableList.copyOf(buildPhases), Objects.requireNonNull(buildConfigurationList, "'buildConfigurations' must not be null"), Objects.requireNonNull(productName, "'productName' must not be null"), Objects.requireNonNull(productReference, "'productReference' must not be null"), ImmutableList.copyOf(dependencies), ImmutableList.copyOf(packageProductDependencies));
		}
	}
}
