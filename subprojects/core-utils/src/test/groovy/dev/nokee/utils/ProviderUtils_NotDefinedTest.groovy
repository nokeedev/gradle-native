package dev.nokee.utils

import org.gradle.api.internal.provider.MissingValueException
import spock.lang.Specification

import static dev.nokee.utils.ProviderUtils.notDefined

class ProviderUtils_NotDefinedTest extends Specification {
	def "always returns null on getOrNull()"() {
		expect:
		notDefined().getOrNull() == null
	}

	def "throws exception on get()"() {
		when:
		notDefined().get()

		then:
		thrown(MissingValueException)
	}

	def "always returns false on isPresent()"() {
		expect:
		!notDefined().isPresent()
	}

	def "always return the same instance"() {
		expect:
		notDefined() == notDefined()
	}
}
