package dev.nokee.platform.base.internal.variants;

import dev.nokee.model.internal.AbstractDomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.Variant;

public final class VariantConfigurer extends AbstractDomainObjectConfigurer<Variant> {
	public VariantConfigurer(DomainObjectEventPublisher eventPublisher) {
		super(Variant.class, eventPublisher);
	}
}
