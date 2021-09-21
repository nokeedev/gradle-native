/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.core.exec.internal.CommandLineToolOutputStreamsIntertwineImpl
import spock.lang.Specification
import spock.lang.Subject

@Subject(CommandLineToolOutputStreamsIntertwineImpl)
class CommandLineToolOutputStreamIntertwineImplTest extends Specification {
	def streams = new CommandLineToolOutputStreamsIntertwineImpl()
	def out = new PrintWriter(streams.standardOutput)
	def err = new PrintWriter(streams.errorOutput)

	void write(PrintWriter writer, String s) {
		writer.print(s)
		writer.flush()
	}

	void writeln(PrintWriter writer, String s) {
		writer.println(s)
		writer.flush()
	}

	def "new instance has empty log contents"() {
		expect:
		streams.errorOutputContent.asString == ''
		streams.standardOutputContent.asString == ''
		streams.outputContent.asString == ''
	}

	def "can capture stdout"() {
		when:
		writeln(out, 'Hello, world!')

		then:
		streams.standardOutputContent.asString == "Hello, world!${System.lineSeparator()}"
		streams.errorOutputContent.asString == ''
		streams.outputContent.asString == "Hello, world!${System.lineSeparator()}"
	}

	def "can capture stderr"() {
		when:
		writeln(err, 'Hello, world!')

		then:
		streams.standardOutputContent.asString == ''
		streams.errorOutputContent.asString == "Hello, world!${System.lineSeparator()}"
		streams.outputContent.asString == "Hello, world!${System.lineSeparator()}"
	}

	def "can decouple stdout/stderr per lines"() {
		when:
		writeln(out, 'Hello, world!')
		writeln(err, 'Goodbye, world!')
		writeln(out, 'Hey, world!')
		writeln(err, 'Oh, world!')

		then:
		streams.standardOutputContent.asString == "Hello, world!${System.lineSeparator()}Hey, world!${System.lineSeparator()}"
		streams.errorOutputContent.asString == "Goodbye, world!${System.lineSeparator()}Oh, world!${System.lineSeparator()}"
		streams.outputContent.asString == "Hello, world!${System.lineSeparator()}Goodbye, world!${System.lineSeparator()}Hey, world!${System.lineSeparator()}Oh, world!${System.lineSeparator()}"
	}

	def "can decouple stdout/stderr"() {
		when:
		write(out, 'Hello, world!')
		write(err, 'Goodbye, world!')
		writeln(out, 'Hey, world!')
		writeln(err, 'Oh, world!')

		then:
		streams.standardOutputContent.asString == "Hello, world!Hey, world!${System.lineSeparator()}"
		streams.errorOutputContent.asString == "Goodbye, world!Oh, world!${System.lineSeparator()}"
		streams.outputContent.asString == "Hello, world!Goodbye, world!Hey, world!${System.lineSeparator()}Oh, world!${System.lineSeparator()}"
	}

	def "only commit captured output on flush"() {
		when:
		out.print('Hello, ')
		err.print('Goodbye, ')
		out.println('world!')
		err.println('world!')
		err.flush()
		out.flush()

		then:
		streams.standardOutputContent.asString == "Hello, world!${System.lineSeparator()}"
		streams.errorOutputContent.asString == "Goodbye, world!${System.lineSeparator()}"
		streams.outputContent.asString == "Goodbye, world!${System.lineSeparator()}Hello, world!${System.lineSeparator()}"
	}
}
