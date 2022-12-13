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

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.project.CodeableXCBuildConfiguration;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.Optional;
import java.util.function.Consumer;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Build configuration containing a file reference to a xcconfig file and additional inline settings.
 */
public interface XCBuildConfiguration extends PBXBuildStyle {
	Optional<PBXFileReference> getBaseConfigurationReference();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<XCBuildConfiguration>, LenientAwareBuilder<Builder> {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "XCBuildConfiguration");
			builder.requires(key(CodeableXCBuildConfiguration.CodingKeys.name));

			// Default values
			builder.put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, BuildSettings.empty());
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		public Builder name(String name) {
			builder.put(CodeableXCBuildConfiguration.CodingKeys.name, name);
			return this;
		}

		public Builder buildSettings(Consumer<? super BuildSettings.Builder> builderConsumer) {
			final BuildSettings.Builder builder = BuildSettings.builder();
			builderConsumer.accept(builder);
			return buildSettings(builder.build());
		}

		public Builder baseConfigurationReference(PBXFileReference baseConfigurationReference) {
			builder.put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, baseConfigurationReference);
			return this;
		}

		public Builder buildSettings(BuildSettings buildSettings) {
			builder.put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, buildSettings);
			return this;
		}

		@Override
		public XCBuildConfiguration build() {
			return new CodeableXCBuildConfiguration(builder.build());
		}
	}
}
