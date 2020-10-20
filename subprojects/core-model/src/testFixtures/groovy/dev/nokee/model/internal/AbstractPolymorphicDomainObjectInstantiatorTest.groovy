package dev.nokee.model.internal

import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.InvalidUserDataException
import spock.lang.Specification

abstract class AbstractPolymorphicDomainObjectInstantiatorTest<T> extends Specification {
	protected PolymorphicDomainObjectInstantiator<T> newSubject() {
		return newSubject('test instantiator')
	}

	protected abstract PolymorphicDomainObjectInstantiator<T> newSubject(String displayName)

	protected abstract Class<T> getBaseType()

	protected abstract Class<? extends T> getChildType()

	protected abstract List<Class<? extends T>> registerKnownTypes(PolymorphicDomainObjectInstantiator<T> subject)

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

	def "does not throw an exception when a factory already exists for the type if not absent"() {
		given:
		def subject = newSubject()
		subject.registerFactory(childType, Stub(DomainObjectFactory))

		when:
		subject.registerFactoryIfAbsent(childType, Stub(DomainObjectFactory))

		then:
		noExceptionThrown()
	}

	def "throws an exception if factory is absent for given type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(Stub(DomainObjectIdentifier), childType)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message.startsWith("Cannot create a ${childType.simpleName} because this type is not known to test instantiator.")
	}

	def "mentions supported types when instantiating unregistered type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(Stub(DomainObjectIdentifier), childType)
		then:
		def ex1 = thrown(InvalidUserDataException)
		ex1.message.endsWith("Known types are: (None)")

		when:
		def knownTypes = registerKnownTypes(subject)
		subject.newInstance(Stub(DomainObjectIdentifier), childType)
		then:
		def ex2 = thrown(InvalidUserDataException)
		ex2.message.endsWith("Known types are: ${knownTypes.collect { it.simpleName }.join(', ')}")
	}

	def "can instantiate registered type"() {
		given:
		def subject = newSubject()
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(childType, factory)
		def identifier = Stub(DomainObjectIdentifier)

		when:
		def result = subject.newInstance(identifier, childType)

		then:
		childType.isAssignableFrom(result.class)
		1 * factory.create(identifier) >> Stub(childType)
		0 * factory._
	}

	def "returns no creatable types on newly created instantitor"() {
		when:
		def subject = newSubject()

		then:
		subject.creatableTypes == [] as Set
	}

	def "can get creatable types of the polymorphic instantiator"() {
		given:
		def subject = newSubject()

		when:
		def knownTypes = registerKnownTypes(subject)

		then:
		subject.creatableTypes == knownTypes as Set
	}

	def "can assert a type is creatable by the polymorphic instantiator"() {
		given:
		def subject = newSubject()

		and:
		def knownTypes = registerKnownTypes(subject)

		when:
		knownTypes.each { subject.assertCreatableType(it) }
		then:
		noExceptionThrown()

		when:
		subject.assertCreatableType(childType)
		then:
		def ex = thrown(InvalidUserDataException)
		ex.message.endsWith("Known types are: ${knownTypes.collect { it.simpleName }.join(', ')}")
	}

	interface MyIncompatibleType {}
}
