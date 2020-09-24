package dev.nokee.platform.base.internal.dependencies

import spock.lang.Subject

@Subject(ConfigurationBucketRegistryImpl)
class ConfigurationBucketRegistry_CreateIntegrationTest extends ConfigurationBucketRegistry_AbstractCreateIntegrationTest {
	@Override
	protected create(ConfigurationBucketRegistryImpl subject, String name, ConfigurationBucketType type) {
		return subject.createIfAbsent(name, type)
	}
}
