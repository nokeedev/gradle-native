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
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * List of build configurations.
 */
public final class XCConfigurationList extends PBXProjectItem {
    private final ImmutableMap<String, XCBuildConfiguration> buildConfigurationsByName;
    @Nullable private final String defaultConfigurationName;
    private final boolean defaultConfigurationIsVisible;

	private XCConfigurationList(ImmutableMap<String, XCBuildConfiguration> buildConfigurations, @Nullable String defaultConfigurationName, DefaultConfigurationVisibility defaultConfigurationVisibility) {
		this.defaultConfigurationName = defaultConfigurationName;
		defaultConfigurationIsVisible = defaultConfigurationVisibility == DefaultConfigurationVisibility.VISIBLE;

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
		private final ImmutableMap.Builder<String, XCBuildConfiguration> buildConfigurations = ImmutableMap.builder();
		private DefaultConfigurationVisibility defaultConfigurationVisibility = DefaultConfigurationVisibility.HIDDEN;
		private String defaultConfigurationName;

		public Builder buildConfiguration(Consumer<? super XCBuildConfiguration.Builder> builderConsumer) {
			final XCBuildConfiguration.Builder builder = XCBuildConfiguration.builder();
			builderConsumer.accept(builder);
			final XCBuildConfiguration buildConfiguration = builder.build();
			this.buildConfigurations.put(buildConfiguration.getName(), buildConfiguration);
			return this;
		}

		public Builder defaultConfigurationName(String name) {
			this.defaultConfigurationName = name;
			return this;
		}

		public Builder defaultConfigurationVisibility(DefaultConfigurationVisibility visibility) {
			this.defaultConfigurationVisibility = visibility;
			return this;
		}

		public XCConfigurationList build() {
			return new XCConfigurationList(buildConfigurations.build(), defaultConfigurationName, defaultConfigurationVisibility);
		}
	}

	public enum DefaultConfigurationVisibility {
		HIDDEN,
		VISIBLE;
	}
}
