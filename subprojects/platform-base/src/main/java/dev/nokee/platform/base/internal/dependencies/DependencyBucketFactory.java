package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.DependencyBucket;

public interface DependencyBucketFactory<T extends DependencyBucket> extends DomainObjectFactory<T> {
	@Override
	@SuppressWarnings("unchecked")
	default T create(DomainObjectIdentifier identifier) {
		return create((DependencyIdentifier<T>) identifier);
	}

	T create(DependencyIdentifier<T> identifier);
}
