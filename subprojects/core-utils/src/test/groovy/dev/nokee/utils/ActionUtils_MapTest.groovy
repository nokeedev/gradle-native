package dev.nokee.utils

import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ActionUtils.doNothing

@Subject(ActionUtils)
class ActionUtils_MapTest extends Specification {
	def "executes action with mapped value"() {
		given:
		def action = Mock(Action)
		def subject = ActionUtils.map({ it.size() }, action)

		when:
		subject.execute("obj")
		then:
		1 * action.execute(3)

		when:
		subject.execute("foobar")
		then:
		1 * action.execute(6)
	}

	def "action toString() explains where the action comes from"() {
		given:
		def mapper = { it.size() }

		expect:
		ActionUtils.map(mapper, doNothing()).toString() == "ActionUtils.map(${mapper.toString()}, ActionUtils.doNothing())"
	}
}
