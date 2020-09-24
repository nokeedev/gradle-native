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
