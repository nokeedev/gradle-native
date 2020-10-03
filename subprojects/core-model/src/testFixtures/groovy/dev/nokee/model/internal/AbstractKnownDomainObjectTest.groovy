package dev.nokee.model.internal

abstract class AbstractKnownDomainObjectTest<T> extends AbstractDeferrableDomainObjectTest<T> {
	@Override
	protected <S extends T> KnownDomainObject<S> newSubject(TypeAwareDomainObjectIdentifier<S> identifier) {
		return newEntityFactory().create(identifier)
	}
}
