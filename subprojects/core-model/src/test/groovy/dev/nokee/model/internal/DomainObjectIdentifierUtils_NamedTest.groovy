package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.named

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_NamedTest extends Specification {
	def "returns false for non-name aware identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!named("foo").test(identifier)
	}

	def "returns true when identifier name matches"() {
		given:
		def identifier = Mock(NameAwareDomainObjectIdentifier)

		when:
		assert named('foo').test(identifier)
		then:
		1 * identifier.name >> 'foo'

		when:
		assert named('bar').test(identifier)
		then:
		1 * identifier.name >> 'bar'

		when:
		assert !named('far').test(identifier)
		then:
		1 * identifier.name >> 'foo'
	}

	def "can compare predicates"() {
		expect:
		named('foo') == named('foo')
		named('foo') != named('bar')
	}

	def "predicate toString() explains where it comes from"() {
		expect:
		named('foo').toString() == "DomainObjectIdentifierUtils.named(foo)"
		named('bar').toString() == "DomainObjectIdentifierUtils.named(bar)"
		named('far').toString() == "DomainObjectIdentifierUtils.named(far)"
	}
}
