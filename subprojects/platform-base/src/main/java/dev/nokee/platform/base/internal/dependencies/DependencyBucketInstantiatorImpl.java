package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.internal.PolymorphicDomainObjectInstantiator;
import dev.nokee.platform.base.DependencyBucket;

import java.util.Set;

final class DependencyBucketInstantiatorImpl implements DependencyBucketInstantiator {
	private final PolymorphicDomainObjectInstantiator<DependencyBucket> delegate = new PolymorphicDomainObjectInstantiator<>(DependencyBucket.class, "dependency bucket");

	@Override
	public <T extends DependencyBucket> T newInstance(DependencyIdentifier<T> identifier, Class<T> type) {
		return delegate.newInstance(identifier, type);
	}

	@Override
	public Set<? extends Class<? extends DependencyBucket>> getCreatableTypes() {
		return delegate.getCreatableTypes();
	}

	@Override
	public <U extends DependencyBucket> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		delegate.registerFactory(type, factory);
	}
}
