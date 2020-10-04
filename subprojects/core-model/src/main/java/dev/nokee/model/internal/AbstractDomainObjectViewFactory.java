package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;

public abstract class AbstractDomainObjectViewFactory<T> implements DomainObjectViewFactory<T> {
	private final Class<T> baseType;

	protected AbstractDomainObjectViewFactory(Class<T> baseType) {
		this.baseType = baseType;
	}

	public DomainObjectView<T> create(DomainObjectIdentifier viewOwner) {
		return create(viewOwner, baseType);
	}

	public abstract <S extends T> DomainObjectView<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType);
}
