package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.KnownDomainObject;
import dev.nokee.model.internal.KnownDomainObjectFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;

import javax.inject.Provider;

public final class KnownComponentFactory implements KnownDomainObjectFactory<Component> {
	private final Provider<ComponentRepository> repositoryProvider;
	private final Provider<ComponentConfigurer> configurerProvider;

	public KnownComponentFactory(Provider<ComponentRepository> repositoryProvider, Provider<ComponentConfigurer> configurerProvider) {
		this.repositoryProvider = repositoryProvider;
		this.configurerProvider = configurerProvider;
	}

	public <T extends Component> KnownComponent<T> create(ComponentIdentifier<T> identifier) {
		return new KnownComponent<>(identifier, repositoryProvider.get().identified(identifier), configurerProvider.get());
	}

	@Override
	public <S extends Component> KnownDomainObject<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return create((ComponentIdentifier<S>) identifier);
	}
}
