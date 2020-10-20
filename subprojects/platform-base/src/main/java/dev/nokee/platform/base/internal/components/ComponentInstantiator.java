package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.AbstractPolymorphicDomainObjectInstantiator;
import dev.nokee.platform.base.Component;

public final class ComponentInstantiator extends AbstractPolymorphicDomainObjectInstantiator<Component> {
	public ComponentInstantiator(String displayName) {
		super(Component.class, displayName);
	}
}
