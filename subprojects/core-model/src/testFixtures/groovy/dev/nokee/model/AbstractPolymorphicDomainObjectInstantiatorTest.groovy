package dev.nokee.model

import spock.lang.Specification

abstract class AbstractPolymorphicDomainObjectInstantiatorTest extends Specification {
	protected abstract def newSubject()
	protected abstract Set<Class<?>> registerKnownTypes(def subject)

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
}
