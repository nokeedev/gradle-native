/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.core.exec

import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.Callable

import static dev.nokee.core.exec.CommandLine.script
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
		thrown(NullPointerException)

		when:
		CommandLine.of([null])
		then:
		thrown(NullPointerException)

		when:
		CommandLine.of(['foo', null])
		then:
		thrown(NullPointerException)
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

	@Requires({ SystemUtils.IS_OS_WINDOWS })
	def "can create command line for cmd on Windows"() {
		expect:
		def scriptCommand = script('dir', 'C:\\some\\path')
		scriptCommand.tool.executable == 'cmd'
		scriptCommand.arguments.get() == ['/c', 'dir C:\\some\\path']
	}

	@Requires({ !SystemUtils.IS_OS_WINDOWS })
	def "can create command line for cmd on *nix"() {
		expect:
		def scriptCommand = script('ls', '-l', '/some/path')
		scriptCommand.tool.executable == '/bin/bash'
		scriptCommand.arguments.get() == ['-c', 'ls -l /some/path']
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
