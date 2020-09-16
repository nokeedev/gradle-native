package dev.nokee.utils

import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ActionUtils.doNothing
import static dev.nokee.utils.TransformerUtils.configureInPlace

@Subject(TransformerUtils)
class TransformerUtils_ConfigureInPlaceTest extends Specification {
	def "transformer returns the input value"() {
		expect:
		configureInPlace({}).transform('foo') == 'foo'
		configureInPlace({}).transform(['a', 'b', 'c']) == ['a', 'b', 'c']
		configureInPlace({}).transform(42) == 42
	}

	def "transformer calls the action with input value"() {
		given:
		def action = Mock(Action)

		when:
		configureInPlace(action).transform('foo')
		configureInPlace(action).transform(['a', 'b', 'c'])
		configureInPlace(action).transform(42)

		then:
		1 * action.execute('foo')
		1 * action.execute(['a', 'b', 'c'])
		1 * action.execute(42)
	}

	def "transformer toString() explains where the transformer comes from"() {
		expect:
		configureInPlace(doNothing()).toString() == 'TransformerUtils.configureInPlace(ActionUtils.doNothing())'
	}
}
