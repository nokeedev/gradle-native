package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;

public interface DependencyBucketFactory {
	DependencyBucket create(String name);
}
