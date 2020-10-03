package dev.nokee.platform.base.internal.components;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.DomainObjectProviderFactory;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;

public final class ComponentProviderFactory implements DomainObjectProviderFactory<Component> {
	private final ComponentRepository repository;
	private final ComponentConfigurer configurer;

	public ComponentProviderFactory(ComponentRepository repository, ComponentConfigurer configurer) {
		this.repository = repository;
		this.configurer = configurer;
	}

	@Override
	public <S extends Component> DomainObjectProvider<S> create(TypeAwareDomainObjectIdentifier<S> identifier) {
		return new ComponentProvider<>(identifier, repository.identified(identifier), configurer);
	}
}
