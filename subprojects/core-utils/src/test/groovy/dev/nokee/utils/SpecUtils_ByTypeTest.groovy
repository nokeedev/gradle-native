package dev.nokee.utils

import spock.lang.Specification

import static dev.nokee.utils.SpecUtils.byType
import static dev.nokee.utils.SpecUtils.getTypeFiltered

class SpecUtils_ByTypeTest extends Specification {
	def "can filter types"() {
		expect:
		byType(String).isSatisfiedBy('foo')
		byType(Integer).isSatisfiedBy(42)
		!byType(Integer).isSatisfiedBy(42L)
	}

	def "can extract the filtering type from the spec"() {
		expect:
		getTypeFiltered(byType(String)).present
		getTypeFiltered(byType(String)).get() == String
		getTypeFiltered(byType(Integer)).get() == Integer
	}

	def "non-type filtering spec returns empty optional"() {
		expect:
		!getTypeFiltered({false}).present
	}

	def "spec toString() explains where the spec comes from"() {
		expect:
		byType(String).toString() == 'SpecUtils.byType(java.lang.String)'
	}
}
