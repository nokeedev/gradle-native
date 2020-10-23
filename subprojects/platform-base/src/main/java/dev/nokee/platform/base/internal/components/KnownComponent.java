package dev.nokee.platform.base.internal.components;

import dev.nokee.model.internal.AbstractKnownDomainObject;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import lombok.ToString;
import org.gradle.api.provider.Provider;

@ToString
public final class KnownComponent<T extends Component> extends AbstractKnownDomainObject<Component, T> {
	private final ComponentIdentifier<T> identifier;

	KnownComponent(ComponentIdentifier<T> identifier, Provider<T> provider, ComponentConfigurer configurer) {
		super(identifier, provider, configurer);
		this.identifier = identifier;
	}

	@Override
	public ComponentIdentifier<T> getIdentifier() {
		return identifier;
	}
}
