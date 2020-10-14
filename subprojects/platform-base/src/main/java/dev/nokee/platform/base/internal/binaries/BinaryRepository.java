package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepository;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.platform.base.Binary;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public final class BinaryRepository extends AbstractRealizableDomainObjectRepository<Binary> {
	@Inject
	public BinaryRepository(DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		super(Binary.class, eventPublisher, realizer, providerFactory);
	}
}
