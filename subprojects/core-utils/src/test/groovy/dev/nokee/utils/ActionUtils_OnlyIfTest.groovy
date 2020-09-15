package dev.nokee.utils

import org.gradle.api.Action
import org.gradle.api.specs.Spec
import org.gradle.api.specs.Specs
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ActionUtils.doNothing

@Subject(ActionUtils)
class ActionUtils_OnlyIfTest extends Specification {
	def "executes action only if spec matches"() {
		given:
		def spec = Mock(Spec)
		def action = Mock(Action)
		def subject = ActionUtils.onlyIf(spec, action)
		def obj = new Object()

		when:
		subject.execute(obj)
		then:
		1 * spec.isSatisfiedBy(obj) >> true
		and:
		1 * action.execute(obj)

		when:
		subject.execute(obj)
		then:
		1 * spec.isSatisfiedBy(obj) >> false
		and:
		0 * action.execute(_)
	}

	def "returns doNothing() action for obvious satisfy none spec"() {
		expect:
		ActionUtils.onlyIf(Specs.satisfyNone(), Stub(Action)) == doNothing()
	}

	def "returns specified action for obvious satisfy all spec"() {
		def action = Stub(Action)

		expect:
		ActionUtils.onlyIf(Specs.satisfyAll(), action) == action
	}

	def "executes action only if type matches"() {
		given:
		def action = Mock(Action)
		def subject = ActionUtils.<String, Object>onlyIf(String, action)
		def obj = new Object()

		when:
		subject.execute("obj")
		then:
		1 * action.execute("obj")

		when:
		subject.execute(obj)
		then:
		0 * action.execute(_)
	}

	def "action toString() explains where the action comes from"() {
		given:
		def spec = { true }

		expect:
		ActionUtils.onlyIf(spec, doNothing()).toString() == "ActionUtils.onlyIf(${spec.toString()}, ActionUtils.doNothing())"
		ActionUtils.onlyIf(Object, doNothing()).toString() == 'ActionUtils.onlyIf(java.lang.Object, ActionUtils.doNothing())'
	}
}
