package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ActionUtils.doNothing

@Subject(ActionUtils)
class ActionUtils_DoNothingTest extends Specification {
	def "do nothing action does not throws"() {
		when:
		doNothing().execute(new Object())
		then:
		noExceptionThrown()

		when:
		doNothing().execute(null)
		then:
		noExceptionThrown()
	}

	def "always the same instance"() {
		expect:
		doNothing() == doNothing()
	}

	def "action toString() explains where the action comes from"() {
		expect:
		doNothing().toString() == 'ActionUtils.doNothing()'
	}
}
