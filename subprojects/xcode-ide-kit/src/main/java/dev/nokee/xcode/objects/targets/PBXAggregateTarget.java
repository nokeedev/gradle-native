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
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Target backed by shell scripts or nothing (only specifying dependencies).
 */
public interface PBXAggregateTarget extends PBXTarget {
	@Override
	default void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXAggregateTarget>, BuildPhaseAwareBuilder<Builder>, BuildConfigurationsAwareBuilder<Builder>, LenientAwareBuilder<Builder>, TargetDependenciesAwareBuilder<Builder>, PBXTarget.Builder {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXAggregateTarget.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXAggregateTarget");
			builder.requires(key(CodeablePBXAggregateTarget.CodingKeys.name));
			builder.requires(key(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeablePBXAggregateTarget.CodingKeys.name, name);
			return this;
		}

		@Override
		public PBXTarget.Builder productName(String productName) {
			builder.put(CodeablePBXAggregateTarget.CodingKeys.productName, productName);
			return this;
		}

		@Override
		public Builder buildConfigurations(XCConfigurationList buildConfigurations) {
			builder.put(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, buildConfigurations);
			return this;
		}

		@Override
		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			List<PBXBuildPhase> sanitizedBuildPhases = stream(buildPhases).peek(Objects::requireNonNull).peek(this::assertOnlyRunOrCopyPhase).collect(ImmutableList.toImmutableList());
			builder.put(CodeablePBXAggregateTarget.CodingKeys.buildPhases, sanitizedBuildPhases);
			return this;
		}

		@Override
		public Builder buildPhase(PBXBuildPhase buildPhase) {
			builder.add(CodeablePBXAggregateTarget.CodingKeys.buildPhases, buildPhase);
			return this;
		}

		private void assertOnlyRunOrCopyPhase(PBXBuildPhase buildPhase) {
			if (!(buildPhase instanceof PBXShellScriptBuildPhase) && !(buildPhase instanceof PBXCopyFilesBuildPhase)) {
				throw new RuntimeException(String.format("'%s' is not supported, PBXAggregateTarget supports only PBXShellScriptBuildPhase or PBXCopyFilesBuildPhase", buildPhase.getClass().getSimpleName()));
			}
		}

		@Override
		public Builder dependency(PBXTargetDependency dependency) {
			builder.add(CodeablePBXAggregateTarget.CodingKeys.dependencies, dependency);
			return this;
		}

		public Builder dependencies(Iterable<? extends PBXTargetDependency> dependencies) {
			builder.put(CodeablePBXAggregateTarget.CodingKeys.dependencies, dependencies);
			return this;
		}

		@Override
		public PBXAggregateTarget build() {
			return new CodeablePBXAggregateTarget(builder.build());
		}
	}
}
