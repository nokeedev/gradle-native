package dev.nokee.core.exec

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.Callable

import static java.util.Collections.emptyList

@Subject(CommandLine)
class CommandLineTest extends Specification {
	def "can create command line via static constructor with array"() {
		when:
		def commandLine = CommandLine.of('foo', '-c', 'bar')

		then:
		noExceptionThrown()

		and:
		commandLine.tool.executable == 'foo'
		commandLine.arguments.get() == ['-c', 'bar']
	}

	def "can create command line via static constructor with list"() {
		when:
		def commandLine = CommandLine.of(['foo', '-c', 'bar'])

		then:
		noExceptionThrown()

		and:
		commandLine.tool.executable == 'foo'
		commandLine.arguments.get() == ['-c', 'bar']
	}

	def "throws exception for empty command line"() {
		when:
		CommandLine.of()
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == 'The command line must contain at least one element for the executable'

		when:
		CommandLine.of(emptyList())
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'The command line must contain at least one element for the executable'
	}

	def "throws exception when any command line elements is null"() {
		when:
		CommandLine.of('foo', null)
		then:
		def ex1 = thrown(NullPointerException)
		ex1.message == 'The command line cannot contain null elements'

		when:
		CommandLine.of([null])
		then:
		def ex2 = thrown(NullPointerException)
		ex2.message == 'The command line cannot contain null elements'

		when:
		CommandLine.of(['foo', null])
		then:
		def ex3 = thrown(NullPointerException)
		ex3.message == 'The command line cannot contain null elements'
	}

	def "throws exception when command line list is null"() {
		when:
		CommandLine.of(null)
		then:
		def ex = thrown(NullPointerException)
		ex.message == 'commandLine is marked non-null but is null'
	}

	def "can use callable to provide command line"() {
		expect:
		def c1 = CommandLine.of(callableOf(['cmd', '/c']), 'foo')
		c1.tool.executable == 'cmd'
		c1.arguments == CommandLineToolArguments.of('/c', 'foo')

		and:
		def c2 = CommandLine.of(callableOf('foo'), 'bar')
		c2.tool.executable == 'foo'
		c2.arguments == CommandLineToolArguments.of('bar')
	}

	def "can use multiple list to construct the command line"() {
		expect:
		def c = CommandLine.of(['cmd', '/c'], 'foo', ['arg1', 'arg2', 'arg3'])
		c.tool.executable == 'cmd'
		c.arguments == CommandLineToolArguments.of('/c', 'foo', 'arg1', 'arg2', 'arg3')
	}

	static Callable<List<Object>> callableOf(List<?> values) {
		return new Callable<List<Object>>() {
			@Override
			List<Object> call() throws Exception {
				return values
			}
		}
	}

	static Callable<Object> callableOf(Object value) {
		return new Callable<Object>() {
			@Override
			Object call() throws Exception {
				return value
			}
		}
	}
}
