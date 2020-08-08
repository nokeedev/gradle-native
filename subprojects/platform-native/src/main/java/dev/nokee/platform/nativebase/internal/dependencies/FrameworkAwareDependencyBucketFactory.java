package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactory;

public final class FrameworkAwareDependencyBucketFactory implements DependencyBucketFactory {
	private final DependencyBucketFactory delegate;

	public FrameworkAwareDependencyBucketFactory(DependencyBucketFactory delegate) {
		this.delegate = delegate;
	}

	@Override
	public DependencyBucket create(String name) {
		return new FrameworkAwareDependencyBucket(delegate.create(name));
	}
}
