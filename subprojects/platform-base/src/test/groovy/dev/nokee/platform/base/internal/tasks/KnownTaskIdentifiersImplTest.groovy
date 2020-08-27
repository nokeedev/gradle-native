package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(KnownTaskIdentifiersImpl)
class KnownTaskIdentifiersImplTest extends Specification {
	def "requests known task names from registry"() {
		given:
		def identifier = Mock(DomainObjectIdentifier)
		def registry = Mock(KnownTaskIdentifierRegistry)
		def subject = new KnownTaskIdentifiersImpl(identifier, registry)

		when:
		subject.contains('foo')

		then:
		1 * registry.getTaskNamesFor(identifier) >> ([] as Set)
		0 * _
	}

	def "returns true if task name contained in registry under identifier"() {
		given:
		def identifier = Mock(DomainObjectIdentifier)
		def registry = Mock(KnownTaskIdentifierRegistry)
		def subject = new KnownTaskIdentifiersImpl(identifier, registry)

		when:
		def found = subject.contains('foo')

		then:
		1 * registry.getTaskNamesFor(identifier) >> (['foo', 'bar'] as Set)
		0 * _

		and:
		found
	}

	def "returns false if task name is not contained in registry under identifier"() {
		given:
		def identifier = Mock(DomainObjectIdentifier)
		def registry = Mock(KnownTaskIdentifierRegistry)
		def subject = new KnownTaskIdentifiersImpl(identifier, registry)

		when:
		def found = subject.contains('foo')

		then:
		1 * registry.getTaskNamesFor(identifier) >> (['fooTest', 'barTest'] as Set)
		0 * _

		and:
		!found
	}
}
