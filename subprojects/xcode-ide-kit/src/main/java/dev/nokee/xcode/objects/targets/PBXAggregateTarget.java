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
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;

/**
 * Target backed by shell scripts or nothing (only specifying dependencies).
 */
public interface PBXAggregateTarget extends PBXTarget {
	static Builder builder() {
		return new Builder();
	}

	final class Builder implements BuildPhaseAwareBuilder<Builder>, BuildConfigurationsAwareBuilder<Builder>, LenientAwareBuilder<Builder> {
		private String name;
		private String productName;
		private XCConfigurationList buildConfigurations;
		private final List<PBXBuildPhase> buildPhases = new ArrayList<>();
		private final List<PBXTargetDependency> dependencies = new ArrayList<>();
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXAggregateTarget");
			builder.requires(CodeablePBXAggregateTarget.CodingKeys.name, CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList);
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			this.name = requireNonNull(name);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurations) {
			this.buildConfigurations = requireNonNull(buildConfigurations);
			return this;
		}

		@Override
		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			this.buildPhases.clear();
			stream(buildPhases).peek(Objects::requireNonNull).peek(this::assertOnlyRunOrCopyPhase).forEach(this.buildPhases::add);
			return this;
		}

		@Override
		public Builder buildPhase(PBXBuildPhase buildPhase) {
			Objects.requireNonNull(buildPhase);
			buildPhases.add(buildPhase);
			return this;
		}

		private void assertOnlyRunOrCopyPhase(PBXBuildPhase buildPhase) {
			if (!(buildPhase instanceof PBXShellScriptBuildPhase) && !(buildPhase instanceof PBXCopyFilesBuildPhase)) {
				throw new RuntimeException(String.format("'%s' is not supported, PBXAggregateTarget supports only PBXShellScriptBuildPhase or PBXCopyFilesBuildPhase", buildPhase.getClass().getSimpleName()));
			}
		}

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			this.dependencies.clear();
			dependencies.forEach(this.dependencies::add);
			return this;
		}

		public PBXAggregateTarget build() {
			builder.put(CodeablePBXAggregateTarget.CodingKeys.name, name);
			builder.put(CodeablePBXAggregateTarget.CodingKeys.buildPhases, ImmutableList.copyOf(buildPhases));
			builder.put(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, buildConfigurations);
			builder.put(CodeablePBXAggregateTarget.CodingKeys.dependencies, ImmutableList.copyOf(dependencies));

			return new CodeablePBXAggregateTarget(builder.build());
		}
	}
}
