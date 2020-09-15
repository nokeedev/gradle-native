package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.getParent

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_GetParentTest extends Specification {
	def "returns empty optional for non-internal identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!getParent(identifier).present
	}

	def "returns the optional value from internal identifier"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal)
		def identifier = Mock(DomainObjectIdentifierInternal)

		when:
		def result1 = getParent(identifier)
		then:
		1 * identifier.getParentIdentifier() >> Optional.of(parentIdentifier)
		result1.present
		result1.get() == parentIdentifier

		when:
		def result2 = getParent(identifier)
		then:
		1 * identifier.getParentIdentifier() >> Optional.empty()
		!result2.present
	}
}
