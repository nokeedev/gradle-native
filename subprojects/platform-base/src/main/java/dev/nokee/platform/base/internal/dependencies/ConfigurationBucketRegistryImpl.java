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
package dev.nokee.platform.base.internal.dependencies;

import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

public final class ConfigurationBucketRegistryImpl implements ConfigurationBucketRegistry {
	private final ConfigurationContainer configurationContainer;

	public ConfigurationBucketRegistryImpl(ConfigurationContainer configurationContainer) {
		this.configurationContainer = configurationContainer;
	}

	@Override
	public Configuration createIfAbsent(String name, ConfigurationBucketType type) {
		if (hasConfigurationWithName(name)) {
			val configuration = configurationContainer.getByName(name);
			type.assertConfigured(configuration);
			return configuration;
		}

		return configurationContainer.create(name, type::configure);
	}

	@Override
	public Configuration createIfAbsent(String name, ConfigurationBucketType type, Action<? super Configuration> action) {
		if (hasConfigurationWithName(name)) {
			val configuration = configurationContainer.getByName(name);
			type.assertConfigured(configuration);
			return configuration;
		}

		val configuration = configurationContainer.create(name, type::configure);
		action.execute(configuration);
		return configuration;
	}

	// Avoid triggering container rule which realize objects for nothing.
	private boolean hasConfigurationWithName(String name) {
		for (val element : configurationContainer.getCollectionSchema().getElements()) {
			if (element.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
