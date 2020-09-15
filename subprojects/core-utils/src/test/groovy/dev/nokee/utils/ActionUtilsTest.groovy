package dev.nokee.utils

import dev.nokee.utils.internal.CompositeAction
import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ActionUtils.composite
import static dev.nokee.utils.ActionUtils.doNothing

@Subject(ActionUtils)
class ActionUtilsTest extends Specification {
	def "can compose actions"() {
		def a = Mock(Action)
		def b = Mock(Action)

		expect:
		composite(a) == a
		composite(a, b) instanceof CompositeAction
	}

	def "can execute composite action"() {
		def a = Mock(Action)
		def b = Mock(Action)
		def value = new Object()

		when:
		composite(a, b).execute(value)
		then:
		1 * a.execute(value)
		1 * b.execute(value)
		0 * _
	}

	def "ignores actions that do nothing"() {
		given:
		def action = Mock(Action)

		expect:
		composite(doNothing()) == doNothing()
		composite(doNothing(), doNothing()) == doNothing()
		composite(action, doNothing()) == action
		composite(doNothing(), action) == action
	}
}
