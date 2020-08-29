package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Buildable;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.TaskDependency;

abstract class AbstractDependencyBucket implements DependencyBucket, Buildable {
	private final DependencyIdentifier<? extends DependencyBucket> identifier;
	@Getter(AccessLevel.PACKAGE) final Configuration configuration;

	protected AbstractDependencyBucket(DependencyIdentifier<? extends DependencyBucket> identifier, Configuration configuration) {
		assert identifier != null;
		assert configuration != null;
		this.identifier = identifier;
		this.configuration = configuration;
	}

	@Override
	public DependencyBucketName getName() {
		return identifier.getName();
	}

	@Override
	public void extendsFrom(DependencyBucket... buckets) {
		int i = 0;
		for (DependencyBucket bucket : requireNonNullBuckets(buckets)) {
			requireNonNullBucket(bucket, ++i);
			configuration.extendsFrom(DependencyBucketUtils.asConfiguration(bucket));
		}
	}

	private DependencyBucket[] requireNonNullBuckets(DependencyBucket[] buckets) {
		if (buckets == null) {
			throw new IllegalArgumentException("Unable to extends from the specified buckets because argument #1 is null.");
		}
		return buckets;
	}

	private DependencyBucket requireNonNullBucket(DependencyBucket bucket, int argumentPositon) {
		if (bucket == null) {
			throw new IllegalArgumentException(String.format("Unable to extends from the specified buckets because argument #%d is null.", argumentPositon));
		}
		return bucket;
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return configuration.getBuildDependencies();
	}
}
