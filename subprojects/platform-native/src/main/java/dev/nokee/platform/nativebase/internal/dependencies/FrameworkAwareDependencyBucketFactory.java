package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;

public final class FrameworkAwareDependencyBucketFactory implements DependencyBucketFactory {
	private final DependencyBucketFactory delegate;

	public FrameworkAwareDependencyBucketFactory(DependencyBucketFactory delegate) {
		this.delegate = delegate;
	}

	@Override
	public DependencyBucket create(DependencyBucketIdentifier<?> identifier) {
		return new FrameworkAwareDependencyBucket(delegate.create(identifier));
	}
}
