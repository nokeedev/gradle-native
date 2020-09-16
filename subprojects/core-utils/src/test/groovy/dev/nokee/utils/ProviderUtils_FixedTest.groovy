package dev.nokee.utils

import spock.lang.Specification

class ProviderUtils_FixedTest extends Specification {
	def "can create a fixed value Gradle provider"() {
		expect:
		ProviderUtils.fixed(42).get() == 42
		ProviderUtils.fixed(['a', 'b', 'c']).get() == ['a', 'b', 'c']
		ProviderUtils.fixed('foo').get() == 'foo'
	}

	def "throws exception for null value"() {
		when:
		ProviderUtils.fixed(null)

		then:
		thrown(NullPointerException)
	}
}
