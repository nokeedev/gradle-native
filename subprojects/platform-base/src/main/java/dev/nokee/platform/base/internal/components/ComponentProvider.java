package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.AbstractDomainObjectProvider;
import dev.nokee.model.internal.DomainObjectConfigurer;
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier;
import dev.nokee.platform.base.Component;
import org.gradle.api.provider.Provider;

public final class ComponentProvider<T extends Component> extends AbstractDomainObjectProvider<Component, T> {
	ComponentProvider(TypeAwareDomainObjectIdentifier<T> identifier, Provider<T> provider, DomainObjectConfigurer<Component> configurer) {
		super(identifier, provider, configurer);
	}
}
