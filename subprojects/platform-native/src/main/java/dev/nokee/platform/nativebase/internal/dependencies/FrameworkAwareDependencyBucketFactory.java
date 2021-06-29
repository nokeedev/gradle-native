package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import org.gradle.api.model.ObjectFactory;

public final class FrameworkAwareDependencyBucketFactory implements DependencyBucketFactory {
	private final ObjectFactory objects;
	private final DependencyBucketFactory delegate;

	public FrameworkAwareDependencyBucketFactory(ObjectFactory objects, DependencyBucketFactory delegate) {
		this.objects = objects;
		this.delegate = delegate;
	}

	@Override
	public DependencyBucket create(DependencyBucketIdentifier<?> identifier) {
		return new FrameworkAwareDependencyBucket(objects, delegate.create(identifier));
	}
}
