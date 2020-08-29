package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.artifacts.Configuration;

public final class DependencyBucketUtils {
	private DependencyBucketUtils() {}

	public static Configuration asConfiguration(DependencyBucket bucket) {
		if (bucket instanceof AbstractDependencyBucket) {
			return ((AbstractDependencyBucket) bucket).configuration;
		}
		throw new IllegalArgumentException(String.format("Unable to find the Configuration associated with dependency bucket '%s'.", bucket));
	}
}
