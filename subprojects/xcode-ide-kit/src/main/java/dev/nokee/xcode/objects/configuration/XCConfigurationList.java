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

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.PBXProjectItem;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;

/**
 * List of build configurations.
 */
public final class XCConfigurationList extends PBXProjectItem {
	private final ImmutableMap<String, XCBuildConfiguration> buildConfigurationsByName;
	@Nullable private final String defaultConfigurationName;
	private final boolean defaultConfigurationIsVisible;

	private XCConfigurationList(ImmutableMap<String, XCBuildConfiguration> buildConfigurations, @Nullable String defaultConfigurationName, DefaultConfigurationVisibility defaultConfigurationVisibility) {
		this.defaultConfigurationName = defaultConfigurationName;
		this.defaultConfigurationIsVisible = (defaultConfigurationVisibility == DefaultConfigurationVisibility.VISIBLE);
		this.buildConfigurationsByName = buildConfigurations;
	}

	public Map<String, XCBuildConfiguration> getBuildConfigurationsByName() {
		return buildConfigurationsByName;
	}

	public Optional<String> getDefaultConfigurationName() {
		return Optional.ofNullable(defaultConfigurationName);
	}

	public boolean isDefaultConfigurationIsVisible() {
		return defaultConfigurationIsVisible;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
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
			return new XCConfigurationList(ImmutableMap.copyOf(buildConfigurations), defaultConfigurationName, Objects.requireNonNull(defaultConfigurationVisibility, "'defaultConfigurationVisibility' must not be null"));
		}
	}

	public enum DefaultConfigurationVisibility {
		HIDDEN,
		VISIBLE;
	}
}
