package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;

public interface DomainObjectViewFactory<T> {
	DomainObjectView<T> create(DomainObjectIdentifier viewOwner);

	<S extends T> DomainObjectView<S> create(DomainObjectIdentifier viewOwner, Class<S> elementType);
}
