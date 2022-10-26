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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeableXCConfigurationList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;

/**
 * List of build configurations.
 */
public interface XCConfigurationList extends PBXProjectItem {
	Map<String, XCBuildConfiguration> getBuildConfigurationsByName();

	Optional<String> getDefaultConfigurationName();

	boolean isDefaultConfigurationIsVisible();

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
		private final Map<String, XCBuildConfiguration> buildConfigurations = new LinkedHashMap<>();
		private DefaultConfigurationVisibility defaultConfigurationVisibility = DefaultConfigurationVisibility.HIDDEN;
		private String defaultConfigurationName;

		public Builder buildConfiguration(Consumer<? super XCBuildConfiguration.Builder> builderConsumer) {
			final XCBuildConfiguration.Builder builder = XCBuildConfiguration.builder();
			builderConsumer.accept(builder);
			final XCBuildConfiguration buildConfiguration = builder.build();
			this.buildConfigurations.put(buildConfiguration.getName(), buildConfiguration);
			return this;
		}

		public Builder buildConfigurations(Iterable<? extends XCBuildConfiguration> buildConfigurations) {
			this.buildConfigurations.clear();
			stream(buildConfigurations).map(Objects::requireNonNull).forEach(buildConfiguration -> {
				this.buildConfigurations.put(buildConfiguration.getName(), buildConfiguration);
			});
			return this;
		}

		public Builder defaultConfigurationName(String name) {
			this.defaultConfigurationName = Objects.requireNonNull(name);
			return this;
		}

		public Builder defaultConfigurationVisibility(DefaultConfigurationVisibility visibility) {
			this.defaultConfigurationVisibility = Objects.requireNonNull(visibility);
			return this;
		}

		public XCConfigurationList build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "XCConfigurationList");
			builder.put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, ImmutableList.copyOf(buildConfigurations.values()));
			builder.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, defaultConfigurationName);
			builder.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, (Objects.requireNonNull(defaultConfigurationVisibility, "'defaultConfigurationVisibility' must not be null") == DefaultConfigurationVisibility.VISIBLE));

			return new CodeableXCConfigurationList(builder.build());
		}
	}

	enum DefaultConfigurationVisibility {
		HIDDEN,
		VISIBLE;
	}
}
