package dev.nokee.platform.base.internal.dependencies;

import com.google.common.annotations.VisibleForTesting;
import dev.nokee.platform.base.ConsumableDependencyBucket;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.ResolvableDependencyBucket;
import lombok.Value;
import lombok.val;
import lombok.var;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;

public class DependencyBucketFactoryFactory {
	@VisibleForTesting
	static final ThreadLocal<DependencyBucketInfo> NEXT_DEPENDENCY_BUCKET_INFO = new ThreadLocal<DependencyBucketInfo>();

	private final ConfigurationContainer configurationContainer;
	private final DependencyHandler dependencyHandler;

	@Inject
	public DependencyBucketFactoryFactory(ConfigurationContainer configurationContainer, DependencyHandler dependencyHandler) {
		this.configurationContainer = configurationContainer;
		this.dependencyHandler = dependencyHandler;
	}

	public <T extends DependencyBucket> DependencyBucketFactory<T> wrap(Provider<T> bucketProvider) {
		return new DependencyBucketFactory<T>() {
			@Override
			public T create(DependencyIdentifier<T> identifier) {
				var configuration = configurationContainer.findByName(identifier.getConfigurationName());
				if (configuration == null) {
					configuration = configurationContainer.create(identifier.getConfigurationName());
					type(identifier.getType()).configure(configuration);
					configuration.setDescription(identifier.getDisplayName());
				} else {
					type(identifier.getType()).assertConfigured(configuration);
				}

				return DependencyBucketFactoryFactory.create(identifier, configuration, dependencyHandler, bucketProvider);
			}
		};
	}

	@VisibleForTesting
	public static <T extends DependencyBucket> T create(DependencyIdentifier<T> identifier, Configuration configuration, DependencyHandler dependencies, Provider<T> bucketProvider) {
		try {
			NEXT_DEPENDENCY_BUCKET_INFO.set(new DependencyBucketInfo(identifier, configuration, dependencies));
			return bucketProvider.get();
		} finally {
			NEXT_DEPENDENCY_BUCKET_INFO.set(null);
		}
	}

	static DependencyBucketInfo getNextDependencyBucketInfo() {
		val result = NEXT_DEPENDENCY_BUCKET_INFO.get();
		if (result == null) {
			throw new RuntimeException("Direct instantiation of a Base*DependencyBucket is not permitted.");
		}
		return result;
	}

	@Value
	static class DependencyBucketInfo {
		DependencyIdentifier<?> identifier;
		Configuration configuration;
		DependencyHandler dependencies;
	}

	private static DependencyBucketType type(Class<?> type) {
		if (DeclarableDependencyBucket.class.isAssignableFrom(type)) {
			return DependencyBucketType.DECLARABLE;
		} else if (ConsumableDependencyBucket.class.isAssignableFrom(type)) {
			return DependencyBucketType.CONSUMABLE;
		} else if (ResolvableDependencyBucket.class.isAssignableFrom(type)) {
			return DependencyBucketType.RESOLVABLE;
		}
		throw new RuntimeException();
	}

	private enum DependencyBucketType {
		DECLARABLE(false, false), CONSUMABLE(true, false), RESOLVABLE(false, true);

		private final boolean canBeConsumed;
		private final boolean canBeResolved;

		DependencyBucketType(boolean canBeConsumed, boolean canBeResolved) {
			this.canBeConsumed = canBeConsumed;
			this.canBeResolved = canBeResolved;
		}

		public String getBucketTypeName() {
			return toString().toLowerCase(Locale.CANADA);
		}

		public void configure(Configuration configuration) {
			configuration.setCanBeConsumed(canBeConsumed);
			configuration.setCanBeResolved(canBeResolved);
		}

		public void assertConfigured(Configuration configuration) {
			if (configuration.isCanBeConsumed() != canBeConsumed || configuration.isCanBeResolved() != canBeResolved) {
				throw new IllegalStateException(String.format("Cannot reuse existing configuration named '%s' as a %s bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: %s, canBeResolved: %s], actual: [canBeConsumed: %s, canBeResolved: %s]).", configuration.getName(), getBucketTypeName(), canBeConsumed, canBeResolved, configuration.isCanBeConsumed(), configuration.isCanBeResolved()));
			}
		}
	}
}
