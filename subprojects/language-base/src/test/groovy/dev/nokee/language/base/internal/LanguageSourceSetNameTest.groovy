package dev.nokee.language.base.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(LanguageSourceSetName)
class LanguageSourceSetNameTest extends Specification {
	def "can create language source set name"() {
		expect:
		LanguageSourceSetName.of('c').get() == 'c'
		LanguageSourceSetName.of('cpp').get() == 'cpp'
		LanguageSourceSetName.of('objC').get() == 'objC'
	}

	def "can compare language source set name"() {
		expect:
		LanguageSourceSetName.of('c') == LanguageSourceSetName.of('c')
		LanguageSourceSetName.of('cpp') == LanguageSourceSetName.of('cpp')

		and:
		LanguageSourceSetName.of('c') != LanguageSourceSetName.of('cpp')
	}

	def "assert language source set name is not null"() {
		when:
		LanguageSourceSetName.of(null)

		then:
		thrown(AssertionError)
	}

	def "assert language source set name is not empty"() {
		when:
		LanguageSourceSetName.of('')

		then:
		thrown(AssertionError)
	}
}
