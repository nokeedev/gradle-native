package dev.nokee.model.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(DomainObjectCreated)
class DomainObjectCreatedTest extends Specification {
	def "throws exception when identifier type does not match created object"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> MyEntity
		}
		def object = Stub(AnotherEntity)

		when:
		new DomainObjectCreated<>(identifier, object)

		then:
		thrown(AssertionError)
	}

	def "does not throw exception when identifier type matches created object"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> MyEntity
		}
		def object = Stub(MyEntity)

		when:
		new DomainObjectCreated<>(identifier, object)

		then:
		noExceptionThrown()
	}

	interface MyEntity {}
	interface AnotherEntity {}
}
