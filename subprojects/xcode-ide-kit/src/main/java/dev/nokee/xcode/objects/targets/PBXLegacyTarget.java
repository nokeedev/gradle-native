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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build system.
 */
public final class PBXLegacyTarget extends PBXTarget {
	private static final ImmutableList<PBXBuildPhase> ALWAYS_NO_BUILD_PHASES = ImmutableList.of();
	private final String buildArgumentsString;
	private final String buildToolPath;
	private final String buildWorkingDirectory;
	private final boolean passBuildSettingsInEnvironment;

	private PBXLegacyTarget(String name, ProductType productType, String productName, PBXFileReference productReference, XCConfigurationList buildConfigurationList, ImmutableList<PBXTargetDependency> dependencies, String buildArgumentsString, String buildToolPath, String buildWorkingDirectory, boolean passBuildSettingsInEnvironment) {
		super(name, productType, ALWAYS_NO_BUILD_PHASES, buildConfigurationList, productName, productReference, dependencies);
		this.buildArgumentsString = buildArgumentsString;
		this.buildToolPath = buildToolPath;
		this.buildWorkingDirectory = buildWorkingDirectory;
		this.passBuildSettingsInEnvironment = passBuildSettingsInEnvironment;
	}

	public String getBuildArgumentsString() {
		return buildArgumentsString;
	}

	public String getBuildToolPath() {
		return buildToolPath;
	}

	public String getBuildWorkingDirectory() {
		return buildWorkingDirectory;
	}

	public boolean isPassBuildSettingsInEnvironment() {
		return passBuildSettingsInEnvironment;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private ProductType productType;
		private String buildArgumentsString = "$(ACTION)";
		private String buildToolPath = "/usr/bin/make";
		private String buildWorkingDirectory;
		private boolean passBuildSettingsInEnvironment = true;
		private XCConfigurationList buildConfigurationList;
		private String productName;
		private PBXFileReference productReference;
		private final List<PBXTargetDependency> dependencies = new ArrayList<>();

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder productType(ProductType productType) {
			this.productType = Objects.requireNonNull(productType);
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

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			this.dependencies.clear();
			stream(dependencies).map(Objects::requireNonNull).forEach(this.dependencies::add);
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

		public Builder buildArguments(String buildArguments) {
			this.buildArgumentsString = Objects.requireNonNull(buildArguments);
			return this;
		}

		public Builder buildToolPath(String buildToolPath) {
			this.buildToolPath = Objects.requireNonNull(buildToolPath);
			return this;
		}

		public Builder buildWorkingDirectory(String buildWorkingDirectory) {
			this.buildWorkingDirectory = Objects.requireNonNull(buildWorkingDirectory);
			return this;
		}

		public Builder passBuildSettingsInEnvironment(boolean passBuildSettingsInEnvironment) {
			this.passBuildSettingsInEnvironment = passBuildSettingsInEnvironment;
			return this;
		}

		public PBXLegacyTarget build() {
			// TODO: Null checks
			return new PBXLegacyTarget(name, productType, productName, productReference, buildConfigurationList, ImmutableList.copyOf(dependencies), buildArgumentsString, buildToolPath, buildWorkingDirectory, passBuildSettingsInEnvironment);
		}
	}
}
