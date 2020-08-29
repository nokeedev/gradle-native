package dev.nokee.model

import org.gradle.api.InvalidUserDataException
import spock.lang.Specification

abstract class AbstractDomainObjectInstantiatorTest<T extends DomainObjectInstantiator> extends Specification {
	protected abstract T newSubject()
	protected abstract Class<?> getChildType()
	protected abstract String getDisplayName()
	protected abstract DomainObjectIdentifier identifier()
	protected abstract Set<Class<?>> registerKnownTypes(T subject)
	protected abstract void registerType(T subject, Class<?> type, DomainObjectFactory<?> factory)

	def "throws an exception if factory is absent for given type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(identifier(), childType)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message.startsWith("Cannot create a ${childType.simpleName} because this type is not known to ${displayName}.")
	}

	def "mentions supported types when instantiating unregistered type"() {
		given:
		def subject = newSubject()

		when:
		subject.newInstance(identifier(), childType)
		then:
		def ex1 = thrown(InvalidUserDataException)
		ex1.message.endsWith("Known types are: (None)")

		when:
		def knownTypes = registerKnownTypes(subject)
		subject.newInstance(identifier(), childType)
		then:
		def ex2 = thrown(InvalidUserDataException)
		ex2.message.endsWith("Known types are: ${knownTypes.collect { it.simpleName }.join(', ')}")
	}

	def "can instantiate registered type"() {
		given:
		def subject = newSubject()
		def factory = Mock(DomainObjectFactory)
		registerType(subject, childType, factory)
		def identifier = identifier()

		when:
		def result = subject.newInstance(identifier, childType as Class<Object>)

		then:
		childType.isAssignableFrom(result.class)
		1 * factory.create(identifier) >> Stub(childType)
		0 * factory._
	}
}
