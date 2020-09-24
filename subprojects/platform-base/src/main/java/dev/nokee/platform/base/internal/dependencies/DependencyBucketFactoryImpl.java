package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import lombok.val;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName;
import static dev.nokee.utils.ConfigurationUtils.configureDescription;

public final class DependencyBucketFactoryImpl implements DependencyBucketFactory {
	private final ConfigurationBucketRegistry configurationBucketRegistry;
	private final DependencyHandler dependencyHandler;

	public DependencyBucketFactoryImpl(ConfigurationBucketRegistry configurationBucketRegistry, DependencyHandler dependencyHandler) {
		this.configurationBucketRegistry = configurationBucketRegistry;
		this.dependencyHandler = dependencyHandler;
	}

	@Override
	public DependencyBucket create(DependencyBucketIdentifier<?> identifier) {
		val configuration = configurationBucketRegistry.createIfAbsent(identifier.getConfigurationName(), bucketTypeOf(identifier.getType()), configureDescription(mapDisplayName(identifier)));

		return new DefaultDependencyBucket(identifier.getName().get(), configuration, dependencyHandler);
	}

	private static ConfigurationBucketType bucketTypeOf(Class<?> type) {
		if (DeclarableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.DECLARABLE;
		} else if (ConsumableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.CONSUMABLE;
		} else if (ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			return ConfigurationBucketType.RESOLVABLE;
		}
		throw new RuntimeException();
	}
}
