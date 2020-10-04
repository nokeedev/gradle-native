package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepository;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.Variant;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public final class VariantRepository extends AbstractRealizableDomainObjectRepository<Variant> {
	@Inject
	public VariantRepository(DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		super(Variant.class, eventPublisher, realizer, providerFactory);
	}
}
