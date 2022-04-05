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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Concrete target type representing targets built by xcode itself, rather than an external build
 * system.
 */
public final class PBXNativeTarget extends PBXTarget {
	public PBXNativeTarget(String name, String productType, List<PBXBuildPhase> buildPhases, XCConfigurationList buildConfigurationList, String productName, PBXFileReference productReference) {
		super(name, productType, buildPhases, buildConfigurationList, productName, productReference);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String productType;
		private List<PBXBuildPhase> buildPhases = new ArrayList<>();
		private XCConfigurationList buildConfigurationList;
		private String productName;
		private PBXFileReference productReference;

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder productType(String productType) {
			this.productType = productType;
			return this;
		}

		public Builder buildPhase(PBXBuildPhase buildPhase) {
			this.buildPhases.add(buildPhase);
			return this;
		}

		public Builder buildPhases(List<PBXBuildPhase> buildPhases) {
			this.buildPhases.clear();
			this.buildPhases.addAll(buildPhases);
			return this;
		}

		public Builder buildConfigurations(Consumer<? super XCConfigurationList.Builder> builderConsumer) {
			final XCConfigurationList.Builder builder = XCConfigurationList.builder();
			builderConsumer.accept(builder);
			this.buildConfigurationList = builder.build();
			return this;
		}

		public Builder productName(String productName) {
			this.productName = productName;
			return this;
		}

		public Builder productReference(PBXFileReference productReference) {
			this.productReference = productReference;
			return this;
		}

		public PBXNativeTarget build() {
			return new PBXNativeTarget(name, productType, buildPhases, buildConfigurationList, productName, productReference);
		}
	}
}
