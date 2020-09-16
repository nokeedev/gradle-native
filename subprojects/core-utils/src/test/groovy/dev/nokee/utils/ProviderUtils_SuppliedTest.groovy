package dev.nokee.utils

import spock.lang.Specification

class ProviderUtils_SuppliedTest extends Specification {
	def "can create a supplied value Gradle provider"() {
		expect:
		ProviderUtils.supplied({42}).get() == 42
		ProviderUtils.supplied({['a', 'b', 'c']}).get() == ['a', 'b', 'c']
		ProviderUtils.supplied({'foo'}).get() == 'foo'
	}

	def "throws exception for null value"() {
		when:
		ProviderUtils.supplied(null)

		then:
		thrown(NullPointerException)
	}
}
