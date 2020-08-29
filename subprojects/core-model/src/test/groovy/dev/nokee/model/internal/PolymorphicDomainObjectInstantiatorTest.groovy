package dev.nokee.model.internal

import dev.nokee.model.*
import spock.lang.Subject

@Subject(PolymorphicDomainObjectInstantiator)
class PolymorphicDomainObjectInstantiatorTest extends AbstractPolymorphicDomainObjectInstantiatorTest {
	@Override
	protected newSubject() {
		return new PolymorphicDomainObjectInstantiator(MyBaseType, 'test instantiator')
	}

	@Override
	protected Set<Class<?>> registerKnownTypes(Object subject) {
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		return [MyKnownType1, MyKnownType2]
	}

	interface MyBaseType {}

	interface MyKnownType1 extends MyBaseType {}
	interface MyKnownType2 extends MyBaseType {}
}

@Subject(PolymorphicDomainObjectInstantiator)
class PolymorphicDomainObjectInstantiator_FactoryTest extends AbstractDomainObjectFactoryRegistryTest<PolymorphicDomainObjectInstantiator> {
	@Override
	protected PolymorphicDomainObjectInstantiator newSubject() {
		return new PolymorphicDomainObjectInstantiator(MyBaseType, "test instantiator")
	}

	@Override
	protected Class<?> getBaseType() {
		return MyBaseType
	}

	@Override
	protected Class<?> getChildType() {
		return MyChildType
	}

	interface MyBaseType {}

	interface MyChildType extends MyBaseType {}
}

@Subject(PolymorphicDomainObjectInstantiator)
class PolymorphicDomainObjectInstantiator_InstantiatorTest extends AbstractDomainObjectInstantiatorTest<PolymorphicDomainObjectInstantiator> {
	@Override
	protected PolymorphicDomainObjectInstantiator newSubject() {
		return new PolymorphicDomainObjectInstantiator(MyBaseType, "test instantiator")
	}

//	@Override
//	protected Class<?> getBaseType() {
//		return MyBaseType
//	}
//
	@Override
	protected Class<?> getChildType() {
		return MyChildType
	}

	@Override
	protected String getDisplayName() {
		return 'test instantiator'
	}

	@Override
	protected Set<Class<?>> registerKnownTypes(PolymorphicDomainObjectInstantiator subject) {
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		return [MyKnownType1, MyKnownType2]
	}

	@Override
	protected void registerType(PolymorphicDomainObjectInstantiator subject, Class<?> type, DomainObjectFactory<?> factory) {
		subject.registerFactory(type, factory)
	}

	@Override
	protected DomainObjectIdentifier identifier() {
		return Stub(DomainObjectIdentifier)
	}

	interface MyBaseType {}

	interface MyChildType extends MyBaseType {}

	interface MyKnownType1 extends MyBaseType {}
	interface MyKnownType2 extends MyBaseType {}
}
