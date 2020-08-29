package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.DomainObjectFactoryRegistry;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectInstantiator;
import dev.nokee.platform.base.DependencyBucket;

import java.util.Set;

public interface DependencyBucketInstantiator extends DomainObjectInstantiator<DependencyBucket>, DomainObjectFactoryRegistry<DependencyBucket> {
	@Override
	@SuppressWarnings("unchecked")
	default <S extends DependencyBucket> S newInstance(DomainObjectIdentifier identifier, Class<S> type) {
		return newInstance((DependencyIdentifier<S>)identifier, type);
	}

	<S extends DependencyBucket> S newInstance(DependencyIdentifier<S> identifier, Class<S> type);

	Set<? extends Class<? extends DependencyBucket>> getCreatableTypes();
}
