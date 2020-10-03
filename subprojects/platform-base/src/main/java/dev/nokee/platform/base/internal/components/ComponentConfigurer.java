package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.AbstractDomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.Component;

public final class ComponentConfigurer extends AbstractDomainObjectConfigurer<Component> {
	public ComponentConfigurer(DomainObjectEventPublisher eventPublisher) {
		super(Component.class, eventPublisher);
	}
}
