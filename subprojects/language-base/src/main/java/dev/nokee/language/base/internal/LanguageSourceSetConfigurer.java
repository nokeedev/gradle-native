package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.AbstractDomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectEventPublisher;

public final class LanguageSourceSetConfigurer extends AbstractDomainObjectConfigurer<LanguageSourceSet> {
	public LanguageSourceSetConfigurer(DomainObjectEventPublisher eventPublisher) {
		super(LanguageSourceSet.class, eventPublisher);
	}
}
