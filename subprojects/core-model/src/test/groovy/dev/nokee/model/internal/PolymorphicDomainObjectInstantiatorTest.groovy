package dev.nokee.model.internal

import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.InvalidUserDataException
import spock.lang.Specification
import spock.lang.Subject

@Subject(PolymorphicDomainObjectInstantiator)
class PolymorphicDomainObjectInstantiatorTest extends Specification {
	protected PolymorphicDomainObjectInstantiator newSubject() {
		return new PolymorphicDomainObjectInstantiator(MyBaseType, "test instantiator")
	}

	def "can create instantiator without exception"() {
		when:
		newSubject()

		then:
		noExceptionThrown()
	}

	def "throws an exception when registering a factory for an incompatible type"() {
		given:
		def subject = newSubject()
		assert !MyBaseType.isAssignableFrom(MyIncompatibleType)

		when:
		subject.registerFactory(MyIncompatibleType, Stub(DomainObjectFactory))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot register a factory for type MyIncompatibleType because it is not a subtype of container element type ${MyBaseType.simpleName}."
	}

	def "throws an exception when a factory already exists for the type"() {
		given:
		def subject = newSubject()
		subject.registerFactory(MyChildType, Stub(DomainObjectFactory))

		when:
		subject.registerFactory(MyChildType, Stub(DomainObjectFactory))

		then:
		def ex = thrown(RuntimeException)
		ex.message == "Cannot register a factory for type ${MyChildType.simpleName} because a factory for this type is already registered."
	}

	def "throws an exception if factory is absent for given type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(Stub(DomainObjectIdentifier), MyChildType)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message.startsWith("Cannot create a ${MyChildType.simpleName} because this type is not known to test instantiator.")
	}

	def "mentions supported types when instantiating unregistered type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(Stub(DomainObjectIdentifier), MyChildType)
		then:
		def ex1 = thrown(InvalidUserDataException)
		ex1.message.endsWith("Known types are: (None)")

		when:
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		def knownTypes = [MyKnownType1, MyKnownType2]
		subject.newInstance(Stub(DomainObjectIdentifier), MyChildType)
		then:
		def ex2 = thrown(InvalidUserDataException)
		ex2.message.endsWith("Known types are: ${knownTypes.collect { it.simpleName }.join(', ')}")
	}

	def "can instantiate registered type"() {
		given:
		def subject = newSubject()
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(MyChildType, factory)
		def identifier = Stub(DomainObjectIdentifier)

		when:
		def result = subject.newInstance(identifier, MyChildType)

		then:
		MyChildType.isAssignableFrom(result.class)
		1 * factory.create(identifier) >> Stub(MyChildType)
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
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		def knownTypes = [MyKnownType1, MyKnownType2]

		then:
		subject.creatableTypes == knownTypes as Set
	}

	def "can assert a type is creatable by the polymorphic instantiator"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(MyKnownType1, {Stub(MyKnownType1)})
		subject.registerFactory(MyKnownType2, {Stub(MyKnownType2)})
		def knownTypes = [MyKnownType1, MyKnownType2]

		when:
		subject.assertCreatableType(MyKnownType1)
		then:
		noExceptionThrown()

		when:
		subject.assertCreatableType(MyChildType)
		then:
		def ex = thrown(InvalidUserDataException)
		ex.message.endsWith("Known types are: ${knownTypes.collect { it.simpleName }.join(', ')}")
	}

	interface MyIncompatibleType {}
	interface MyBaseType {}
	interface MyChildType extends MyBaseType {}

	interface MyKnownType1 extends MyBaseType {}
	interface MyKnownType2 extends MyBaseType {}
}
