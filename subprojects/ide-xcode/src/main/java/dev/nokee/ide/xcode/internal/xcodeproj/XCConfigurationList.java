/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.xcode.internal.xcodeproj;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * List of build configurations.
 */
public final class XCConfigurationList extends PBXProjectItem {
    private final LoadingCache<String, XCBuildConfiguration> buildConfigurationsByName;
    private List<XCBuildConfiguration> buildConfigurations;
    private Optional<String> defaultConfigurationName;
    private boolean defaultConfigurationIsVisible;

    public XCConfigurationList() {
        buildConfigurations = Lists.newArrayList();
        defaultConfigurationName = Optional.absent();
        defaultConfigurationIsVisible = false;

        buildConfigurationsByName = CacheBuilder.newBuilder().build(
            new CacheLoader<String, XCBuildConfiguration>() {
                @Override
                public XCBuildConfiguration load(String key) throws Exception {
                    XCBuildConfiguration configuration = new XCBuildConfiguration(key);
                    buildConfigurations.add(configuration);
                    return configuration;
                }
            });
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

	@Override
    public String isa() {
        return "XCConfigurationList";
    }
}
