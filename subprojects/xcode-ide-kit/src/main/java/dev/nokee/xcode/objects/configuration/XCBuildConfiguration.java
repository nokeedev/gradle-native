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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Build configuration containing a file reference to a xcconfig file and additional inline settings.
 */
public final class XCBuildConfiguration extends PBXBuildStyle {
	@Nullable
	private PBXFileReference baseConfigurationReference;

	private XCBuildConfiguration(String name, BuildSettings buildSettings, @Nullable PBXFileReference baseConfigurationReference) {
		super(name, buildSettings);
		this.baseConfigurationReference = baseConfigurationReference;
	}

	public Optional<PBXFileReference> getBaseConfigurationReference() {
		return Optional.ofNullable(baseConfigurationReference);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private BuildSettings buildSettings;
		private PBXFileReference baseConfigurationReference;

		private Builder() {}

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
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
			this.buildSettings = Objects.requireNonNull(buildSettings);
			return this;
		}

		public XCBuildConfiguration build() {
			if (buildSettings == null) {
				buildSettings = BuildSettings.empty();
			}

			return new XCBuildConfiguration(Objects.requireNonNull(name, "'name' must not be null"), buildSettings, baseConfigurationReference);
		}
	}
}
