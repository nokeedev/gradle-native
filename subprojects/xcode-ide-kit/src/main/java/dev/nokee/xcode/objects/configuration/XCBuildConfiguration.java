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
package dev.nokee.xcode.objects.configuration;

import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeableXCBuildConfiguration;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Build configuration containing a file reference to a xcconfig file and additional inline settings.
 */
public interface XCBuildConfiguration extends PBXBuildStyle {
	Optional<PBXFileReference> getBaseConfigurationReference();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<XCBuildConfiguration>, LenientAwareBuilder<Builder> {
		private String name;
		private BuildSettings buildSettings;
		private PBXFileReference baseConfigurationReference;
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "XCBuildConfiguration");
			builder.requires(CodeableXCBuildConfiguration.CodingKeys.name);
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

		public Builder buildSettings(Consumer<? super BuildSettings.Builder> builderConsumer) {
			final BuildSettings.Builder builder = BuildSettings.builder();
			builderConsumer.accept(builder);
			this.buildSettings = builder.build();
			return this;
		}

		public Builder baseConfigurationReference(PBXFileReference baseConfigurationReference) {
			this.baseConfigurationReference = baseConfigurationReference;
			return this;
		}

		public Builder buildSettings(BuildSettings buildSettings) {
			this.buildSettings = requireNonNull(buildSettings);
			return this;
		}

		@Override
		public XCBuildConfiguration build() {
			if (buildSettings == null) {
				buildSettings = BuildSettings.empty();
			}

			builder.put(CodeableXCBuildConfiguration.CodingKeys.name, name);
			builder.put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, buildSettings);
			builder.put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, baseConfigurationReference);

			return new CodeableXCBuildConfiguration(builder.build());
		}
	}
}
