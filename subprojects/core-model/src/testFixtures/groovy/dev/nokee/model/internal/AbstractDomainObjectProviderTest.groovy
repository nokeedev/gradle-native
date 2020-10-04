package dev.nokee.model.internal

import dev.nokee.model.DomainObjectProvider

abstract class AbstractDomainObjectProviderTest<T> extends AbstractDeferrableDomainObjectTest<T> {
	protected <S extends T> DomainObjectProvider<S> newSubject(TypeAwareDomainObjectIdentifier<S> identifier) {
		return newEntityProviderFactory().create(identifier)
	}
}
