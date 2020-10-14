package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.provider.Provider;

@EqualsAndHashCode
public final class RealizableGradleProvider implements RealizableDomainObject {
	private final DomainObjectIdentifier identifier;
	private final Provider<?> provider;
	@EqualsAndHashCode.Exclude private final DomainObjectEventPublisher eventPublisher;

	public RealizableGradleProvider(DomainObjectIdentifier identifier, Provider<?> provider, DomainObjectEventPublisher eventPublisher) {
		assert provider != null;
		this.identifier = identifier;
		this.provider = provider;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void realize() {
		val value = provider.get();
		eventPublisher.publish(new DomainObjectCreated<>(identifier, value));
	}
}
