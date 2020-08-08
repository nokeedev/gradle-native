package dev.nokee

import dev.nokee.utils.ActionUtils
import dev.nokee.utils.internal.NullAction
import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.ChainingAction.doNothing
import static dev.nokee.utils.internal.NullAction.DO_NOTHING

@Subject(ChainingAction)
class ChainingActionTest extends Specification {
	def "can create do nothing chaining action"() {
		expect:
		doNothing() == DO_NOTHING
		doNothing() instanceof ChainingAction
	}

	def "chaining null action returns the null action"() {
		expect:
		doNothing().andThen(ActionUtils.doNothing()) == DO_NOTHING
	}

	def "actions are chained together"() {
		given:
		def a = Mock(Action)
		def b = Mock(Action)
		def value = new Object()

		when:
		ChainingAction.of(a).andThen(b).execute(value)

		then:
		1 * a.execute(value)
		1 * b.execute(value)
		0 * _
	}
}
