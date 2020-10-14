package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.internal.AbstractDomainObjectConfigurer;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.platform.base.Binary;

public final class BinaryConfigurer extends AbstractDomainObjectConfigurer<Binary> {
	public BinaryConfigurer(DomainObjectEventPublisher eventPublisher) {
		super(Binary.class, eventPublisher);
	}
}
