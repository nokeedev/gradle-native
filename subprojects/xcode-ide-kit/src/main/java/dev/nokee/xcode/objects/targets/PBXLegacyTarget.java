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
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build system.
 */
public interface PBXLegacyTarget extends PBXTarget {
	String getBuildArgumentsString();

	String getBuildToolPath();

	String getBuildWorkingDirectory();

	boolean isPassBuildSettingsInEnvironment();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXLegacyTarget>, BuildConfigurationsAwareBuilder<Builder>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXLegacyTarget");

			// Default values
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, "$(ACTION)");
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, "/usr/bin/make");
			builder.put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, true);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.name, requireNonNull(name));
			return this;
		}

		public Builder productType(ProductType productType) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.productType, requireNonNull(productType));
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurationList) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, requireNonNull(buildConfigurationList));
			return this;
		}

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			List<PBXTargetDependency> sanitizedDependencies = stream(dependencies).peek(Objects::requireNonNull).collect(ImmutableList.toImmutableList());
			builder.put(CodeablePBXLegacyTarget.CodingKeys.dependencies, sanitizedDependencies);
			return this;
		}

		public Builder productName(String productName) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.productName, requireNonNull(productName));
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.productReference, requireNonNull(productReference));
			return this;
		}

		public Builder buildArguments(String buildArguments) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, requireNonNull(buildArguments));
			return this;
		}

		public Builder buildToolPath(String buildToolPath) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, requireNonNull(buildToolPath));
			return this;
		}

		public Builder buildWorkingDirectory(String buildWorkingDirectory) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, requireNonNull(buildWorkingDirectory));
			return this;
		}

		public Builder passBuildSettingsInEnvironment(boolean passBuildSettingsInEnvironment) {
			builder.put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, passBuildSettingsInEnvironment);
			return this;
		}

		@Override
		public PBXLegacyTarget build() {
			// TODO: Null checks
			return new CodeablePBXLegacyTarget(builder.build());
		}
	}
}
