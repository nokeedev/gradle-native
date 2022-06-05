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
import dev.nokee.xcode.objects.files.PBXFileReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

/**
 * Target backed by shell scripts or nothing (only specifying dependencies).
 */
public final class PBXAggregateTarget extends PBXTarget {
	private static final ProductType NEVER_HAS_PRODUCT_TYPE = null;
	private static final String NEVER_HAS_PRODUCT_NAME = null;
	private static final PBXFileReference NEVER_HAS_PRODUCT_REFERENCE = null;

	private PBXAggregateTarget(String name, ImmutableList<PBXBuildPhase> buildPhases, XCConfigurationList buildConfigurationList, ImmutableList<PBXTargetDependency> dependencies) {
		super(name, NEVER_HAS_PRODUCT_TYPE, buildPhases, buildConfigurationList, NEVER_HAS_PRODUCT_NAME, NEVER_HAS_PRODUCT_REFERENCE, dependencies);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String productName;
		private XCConfigurationList buildConfigurations;
		private final List<PBXBuildPhase> buildPhases = new ArrayList<>();
		private final List<PBXTargetDependency> dependencies = new ArrayList<>();

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder buildConfigurations(XCConfigurationList buildConfigurations) {
			this.buildConfigurations = Objects.requireNonNull(buildConfigurations);
			return this;
		}

		public Builder buildPhases(Iterable<? extends PBXBuildPhase> buildPhases) {
			this.buildPhases.clear();
			stream(buildPhases).peek(Objects::requireNonNull).peek(this::assertOnlyRunOrCopyPhase).forEach(this.buildPhases::add);
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
			return new PBXAggregateTarget(Objects.requireNonNull(name, "'name' must not be null"), ImmutableList.copyOf(buildPhases), Objects.requireNonNull(buildConfigurations, "'buildConfiguration' must not be null"), ImmutableList.copyOf(dependencies));
		}
	}
}
