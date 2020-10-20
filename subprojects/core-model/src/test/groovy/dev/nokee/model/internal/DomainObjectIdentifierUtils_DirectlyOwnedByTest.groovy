package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_DirectlyOwnedByTest extends Specification {
	def "returns false for same identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!directlyOwnedBy(identifier).test(identifier)
	}

	def "returns true when identifier is direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}

		expect:
		directlyOwnedBy(parentIdentifier).test(childIdentifier)
		directlyOwnedBy(parentIdentifier).test(anotherChildIdentifier)
	}

	def "returns false when identifier is not a direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def indirectIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(indirectIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(indirectIdentifier)
		}

		expect:
		!directlyOwnedBy(parentIdentifier).test(childIdentifier)
		!directlyOwnedBy(parentIdentifier).test(anotherChildIdentifier)
	}

	def "returns false when identifier is not descendent"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}

		expect:
		!directlyOwnedBy(childIdentifier).test(parentIdentifier)
		!directlyOwnedBy(anotherChildIdentifier).test(parentIdentifier)
	}

	def "returns false for non-internal identifier type"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!directlyOwnedBy(childIdentifier).test(identifier)
		!directlyOwnedBy(parentIdentifier).test(identifier)
	}

	def "predicate toString() explains where it comes from"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal)

		expect:
		directlyOwnedBy(identifier).toString() == "DomainObjectIdentifierUtils.directlyOwnedBy(${identifier.toString()})"
	}
}
