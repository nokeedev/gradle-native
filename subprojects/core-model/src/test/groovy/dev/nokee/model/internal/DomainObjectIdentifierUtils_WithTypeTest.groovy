package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.withType

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_WithTypeTest extends Specification {
	def "returns false for non-type aware identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!withType(MyBaseEntity).test(identifier)
	}

	def "returns true when identifier is assignable [base type]"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> MyBaseEntity
		}

		expect:
		withType(MyBaseEntity).test(identifier)
		!withType(MyChildEntity).test(identifier)
		!withType(MyUnrelatedEntity).test(identifier)
	}

	def "returns true when identifier is assignable [child type]"() {
		given:
		def identifier = Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> MyChildEntity
		}

		expect:
		withType(MyBaseEntity).test(identifier)
		withType(MyChildEntity).test(identifier)
		!withType(MyUnrelatedEntity).test(identifier)
	}

	def "predicate toString() explains where it comes from"() {
		expect:
		withType(MyBaseEntity).toString() == "DomainObjectIdentifierUtils.withType(${MyBaseEntity.canonicalName})"
		withType(MyChildEntity).toString() == "DomainObjectIdentifierUtils.withType(${MyChildEntity.canonicalName})"
		withType(MyUnrelatedEntity).toString() == "DomainObjectIdentifierUtils.withType(${MyUnrelatedEntity.canonicalName})"
	}

	interface MyBaseEntity {}
	interface MyChildEntity extends MyBaseEntity {}
	interface MyUnrelatedEntity {}
}
