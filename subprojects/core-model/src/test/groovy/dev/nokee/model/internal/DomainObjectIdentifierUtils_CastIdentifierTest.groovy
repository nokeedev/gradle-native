package dev.nokee.model.internal

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.castIdentifier
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.uncheckedIdentifierCast

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_CastIdentifierTest extends Specification {
	def "can unchecked cast"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier)

		when:
		assert uncheckedIdentifierCast(identifier) == identifier

		then:
		noExceptionThrown()
	}

	def "can cast identifier"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> String
		}

		when:
		assert castIdentifier(String, identifier) == identifier

		then:
		noExceptionThrown()
	}

	def "throws exception when identifier type is not castable"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> Object
		}

		when:
		castIdentifier(String, identifier)

		then:
		def ex = thrown(ClassCastException)
		ex.message == "Failed to cast identifier ${identifier.toString()} of type ${identifier.type.name} to identifier of type ${String.name}."
	}
}
