package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import lombok.val;

public final class DefaultDependencyBucketFactory implements DependencyBucketFactory {
	private final ConfigurationFactory configurationFactory;
	private final DependencyFactory dependencyFactory;

	public DefaultDependencyBucketFactory(ConfigurationFactory configurationFactory, DependencyFactory dependencyFactory) {
		this.configurationFactory = configurationFactory;
		this.dependencyFactory = dependencyFactory;
	}

	@Override
	public DependencyBucket create(String name) {
		val configuration = configurationFactory.create(name);
		val bucket = new DefaultDependencyBucket(name, configuration, dependencyFactory);
		return bucket;
	}
}
