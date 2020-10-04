package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepository;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.Component;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public final class ComponentRepository extends AbstractRealizableDomainObjectRepository<Component> {
	@Inject
	public ComponentRepository(DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		super(Component.class, eventPublisher, realizer, providerFactory);
	}
}
