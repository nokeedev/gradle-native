package dev.nokee.model

import spock.lang.Specification

abstract class AbstractDomainObjectFactoryRegistryTest<T extends DomainObjectFactoryRegistry> extends Specification {
	protected abstract T newSubject()
	protected abstract Class<?> getBaseType()
	protected abstract Class<?> getChildType()

	def "can create instantiator without exception"() {
		when:
		newSubject()

		then:
		noExceptionThrown()
	}

	def "throws an exception when registering a factory for an incompatible type"() {
		given:
		def subject = newSubject()
		assert !baseType.isAssignableFrom(MyIncompatibleType)

		when:
		subject.registerFactory(MyIncompatibleType, Stub(DomainObjectFactory))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot register a factory for type MyIncompatibleType because it is not a subtype of container element type ${baseType.simpleName}."
	}

	def "throws an exception when a factory already exists for the type"() {
		given:
		def subject = newSubject()
		subject.registerFactory(childType, Stub(DomainObjectFactory))

		when:
		subject.registerFactory(childType, Stub(DomainObjectFactory))

		then:
		def ex = thrown(RuntimeException)
		ex.message == "Cannot register a factory for type ${childType.simpleName} because a factory for this type is already registered."
	}

	interface MyIncompatibleType {}
}
