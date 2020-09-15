package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ProviderUtils.map

@Subject(ProviderUtils)
class ProviderUtils_MapTest extends Specification {
	def "can map elements one-to-one"() {
		given:
		def subject = map({ "prefix-${it}" })

		expect:
		subject.transform(['bar', 'foo', 'far']) == ['prefix-bar', 'prefix-foo', 'prefix-far']
	}

	def "can map elements to another type"() {
		given:
		def subject = map({ it.size() })

		expect:
		subject.transform(['bar', 'foobar', 'foobarfar']) == [3, 6, 9]
	}

	def "can map set"() {
		given:
		def subject = map({ "prefix-${it}" })

		expect:
		subject.transform(['bar', 'foo', 'far'] as Set) == ['prefix-bar', 'prefix-foo', 'prefix-far']
	}

	def "returns empty list for empty input set"() {
		given:
		def subject = map({ [it] })

		expect:
		subject.transform([]) == []
	}

	def "transformer toString() explains where it comes from"() {
		given:
		def mapper = { it }
		expect:
		map(mapper).toString() == "ProviderUtils.map(${mapper.toString()})"
	}
}
