package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.DomainObjectCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

class DomainObjectCollectionFactoryImpl implements DomainObjectCollectionFactory {
	private final ObjectFactory objectFactory;
	private final ProviderFactory providerFactory;

	@Inject
	public DomainObjectCollectionFactoryImpl(ObjectFactory objectFactory, ProviderFactory providerFactory) {
		this.objectFactory = objectFactory;
		this.providerFactory = providerFactory;
	}

	@Override
	public <T> DomainObjectCollection<T> create(Class<T> type) {
		return new DefaultDomainObjectCollection<>(type, objectFactory, providerFactory);
	}
}
