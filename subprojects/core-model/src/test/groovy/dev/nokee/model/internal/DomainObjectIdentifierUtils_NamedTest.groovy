package dev.nokee.model.internal


import spock.lang.Specification

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.named

class DomainObjectIdentifierUtils_NamedTest extends Specification {
	def "can create named identifier via factory method"() {
		when:
		def identifier = named('main')

		then:
		identifier instanceof NamedDomainObjectIdentifier
	}

	def "returns name created via the factory method"() {
		expect:
		named('main').name == 'main'
		named('test').name == 'test'
		named('integTest').name == 'integTest'
	}

	def "named identifier implement internal interface"() {
		when:
		def identifier = named('main')

		then:
		identifier instanceof DomainObjectIdentifierInternal
	}

	def "identifier toString() explains where it comes from"() {
		expect:
		named('main').toString() == 'DomainObjectIdentifierUtils.named(main)'
	}
}
