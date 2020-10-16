package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.AbstractRealizableDomainObjectRepository;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import org.gradle.api.provider.ProviderFactory;

public final class LanguageSourceSetRepository extends AbstractRealizableDomainObjectRepository<LanguageSourceSet> {
	public LanguageSourceSetRepository(DomainObjectEventPublisher eventPublisher, RealizableDomainObjectRealizer realizer, ProviderFactory providerFactory) {
		super(LanguageSourceSet.class, eventPublisher, realizer, providerFactory);
	}
}
