package dev.nokee.core.exec

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.core.exec.CommandLineToolArguments.empty
import static dev.nokee.core.exec.CommandLineToolArguments.of

@Subject(CommandLineToolArguments)
class CommandLineToolArgumentsTest extends Specification {
	def "empty arguments contains empty list"() {
		expect:
		empty().get() == []
	}

	def "can create arguments with values"() {
		expect:
		of('foo').get() == ['foo']
		of('foo', 'bar').get() == ['foo', 'bar']
		of('foo', 'bar', 'far').get() == ['foo', 'bar', 'far']
	}

	def "can create arguments with list of values"() {
		expect:
		of(['foo']).get() == ['foo']
		of(['foo', 'bar']).get() == ['foo', 'bar']
		of(['foo', 'bar', 'far']).get() == ['foo', 'bar', 'far']
	}

	def "can compare arguments"() {
		expect:
		of(['foo']) == of('foo')
		of(['foo', 'bar']) == of('foo', 'bar')
		of(['foo', 'bar', 'far']) == of('foo', 'bar', 'far')

		and:
		of('foo') != of('bar')
		of('foo', 'bar') != of('bar', 'foo')

		and:
		of([]) == of()
		of([]) == empty()
		of() == empty()

		and:
		of('foo') != empty()
		of('foo') != of()
		of('foo') != of([])
	}
}
