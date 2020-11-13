package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.DeferUtils.executes

@Subject(DeferUtils)
class DeferUtils_ExecutesTest extends Specification {
	def "does not run on creation"() {
		given:
		def runnable = Mock(Runnable)

		when:
		executes(runnable)

		then:
		0 * runnable.run()
	}

	def "run on resolving the provider"() {
		given:
		def runnable = Mock(Runnable)

		when:
		executes(runnable).getOrNull()

		then:
		1 * runnable.run()
	}

	def "returns empty list from provider"() {
		given:
		def runnable = Mock(Runnable)

		expect:
		executes(runnable).get() instanceof List
		executes(runnable).get().empty
	}
}
