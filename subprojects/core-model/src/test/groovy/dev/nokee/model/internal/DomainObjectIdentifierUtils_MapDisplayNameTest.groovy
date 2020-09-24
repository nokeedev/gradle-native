package dev.nokee.model.internal


import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_MapDisplayNameTest extends Specification {
	def "can compare suppliers"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal)

		expect:
		mapDisplayName(identifier) == mapDisplayName(identifier)
		mapDisplayName(identifier) != mapDisplayName(Stub(DomainObjectIdentifierInternal))
	}

	def "returns identifier display name when getting supplier"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal) {
			getDisplayName() >> 'some display name'
		}

		expect:
		mapDisplayName(identifier).get() == 'some display name'
	}

	def "supplier toString() explains where it comes from"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal)

		expect:
		mapDisplayName(identifier).toString() == "DomainObjectIdentifierUtils.mapDisplayName(${identifier.toString()})"
	}
}
