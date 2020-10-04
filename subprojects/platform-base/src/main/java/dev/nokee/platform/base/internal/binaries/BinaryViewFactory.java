package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.AbstractDomainObjectViewFactory;
import dev.nokee.platform.base.Binary;

import static java.util.Objects.requireNonNull;

public final class BinaryViewFactory extends AbstractDomainObjectViewFactory<Binary> {
	private final BinaryRepository repository;
	private final BinaryConfigurer configurer;

	public BinaryViewFactory(BinaryRepository repository, BinaryConfigurer configurer) {
		super(Binary.class);
		this.repository = repository;
		this.configurer = configurer;
	}

	public BinaryViewImpl<Binary> create(DomainObjectIdentifier viewOwner) {
		return create(viewOwner, Binary.class);
	}

	public <S extends Binary> BinaryViewImpl<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType) {
		return new BinaryViewImpl<>(requireNonNull(viewOwner), elementType, repository, configurer, this);
	}
}
