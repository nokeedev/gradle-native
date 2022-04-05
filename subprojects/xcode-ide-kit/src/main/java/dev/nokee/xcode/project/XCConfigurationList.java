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

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * List of build configurations.
 */
public final class XCConfigurationList extends PBXProjectItem {
    private final LoadingCache<String, XCBuildConfiguration> buildConfigurationsByName;
    private List<XCBuildConfiguration> buildConfigurations;
    private Optional<String> defaultConfigurationName;
    private boolean defaultConfigurationIsVisible;

	private XCConfigurationList(Map<String, XCBuildConfiguration> buildConfigurations, String defaultConfigurationName, DefaultConfigurationVisibility defaultConfigurationVisibility) {
		this.buildConfigurations = Lists.newArrayList();
		this.defaultConfigurationName = Optional.fromNullable(defaultConfigurationName);
		defaultConfigurationIsVisible = defaultConfigurationVisibility == DefaultConfigurationVisibility.VISIBLE;

		buildConfigurationsByName = CacheBuilder.newBuilder().build(
			new CacheLoader<String, XCBuildConfiguration>() {
				@Override
				public XCBuildConfiguration load(String key) throws Exception {
					XCBuildConfiguration configuration = new XCBuildConfiguration(key);
					XCConfigurationList.this.buildConfigurations.add(configuration);
					return configuration;
				}
			});

		buildConfigurationsByName.putAll(buildConfigurations);
		this.buildConfigurations.addAll(buildConfigurations.values());
	}

    public LoadingCache<String, XCBuildConfiguration> getBuildConfigurationsByName() {
        return buildConfigurationsByName;
    }

	public Optional<String> getDefaultConfigurationName() {
		return defaultConfigurationName;
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
