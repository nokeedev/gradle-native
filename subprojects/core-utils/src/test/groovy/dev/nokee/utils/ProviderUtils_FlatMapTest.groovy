package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ProviderUtils.flatMap

@Subject(ProviderUtils)
class ProviderUtils_FlatMapTest extends Specification {
	def "can map elements one-to-one"() {
		given:
		def subject = flatMap({ ["prefix-${it}"] })

		expect:
		subject.transform(['bar', 'foo', 'far']) == ['prefix-bar', 'prefix-foo', 'prefix-far']
	}

	def "can map elements one-to-multiple"() {
		given:
		def subject = flatMap({ ["${it}-1", "${it}-2"] })

		expect:
		subject.transform(['bar', 'foo', 'far']) == ['bar-1', 'bar-2', 'foo-1', 'foo-2', 'far-1', 'far-2']
	}

	def "can map elements one-to-none"() {
		given:
		def subject = flatMap({ (it == 'foo')? [] : [it] })

		expect:
		subject.transform(['bar', 'foo', 'far']) == ['bar', 'far']
	}

	def "can map elements to another type"() {
		given:
		def subject = flatMap({ [it.size()] })

		expect:
		subject.transform(['bar', 'foobar', 'foobarfar']) == [3, 6, 9]
	}

	def "can flat map set"() {
		given:
		def subject = flatMap({ ["prefix-${it}"] })

		expect:
		subject.transform(['bar', 'foo', 'far'] as Set) == ['prefix-bar', 'prefix-foo', 'prefix-far']
	}

	def "returns empty list for empty input set"() {
		given:
		def subject = flatMap({ [it] })

		expect:
		subject.transform([]) == []
	}

	def "transformer toString() explains where it comes from"() {
		given:
		def mapper = {[it]}
		expect:
		flatMap(mapper).toString() == "ProviderUtils.flatMap(${mapper.toString()})"
	}
}
