package dev.nokee.utils

import dev.nokee.utils.internal.CompositeAction
import dev.nokee.utils.internal.NullAction
import org.gradle.api.Action
import org.gradle.internal.Actions
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.ChainingAction.doNothing
import static dev.nokee.ChainingAction.doNothing
import static dev.nokee.utils.ActionUtils.composite
import static dev.nokee.utils.ActionUtils.doNothing
import static dev.nokee.utils.internal.NullAction.DO_NOTHING

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
		composite(doNothing()) == DO_NOTHING
		composite(doNothing(), doNothing()) == DO_NOTHING
		composite(action, doNothing()) == action
		composite(doNothing(), action) == action
	}

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
}
