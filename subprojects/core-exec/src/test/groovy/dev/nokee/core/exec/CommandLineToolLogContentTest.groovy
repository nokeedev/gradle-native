package dev.nokee.core.exec


import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

import static dev.nokee.core.exec.CommandLineToolLogContent.empty
import static dev.nokee.core.exec.CommandLineToolLogContent.of
import static org.fusesource.jansi.Ansi.Color.GREEN
import static org.fusesource.jansi.Ansi.Color.RED
import static org.fusesource.jansi.Ansi.ansi
import static org.fusesource.jansi.AnsiConsole.systemInstall
import static org.fusesource.jansi.AnsiConsole.systemUninstall

@Subject(CommandLineToolLogContent)
class CommandLineToolLogContentTest extends Specification {
	//region default implementation
	def "throws exception when content is null"() {
		when:
		of(null)

		then:
		def ex = thrown(NullPointerException)
		ex.message == "Cannot create log content from null."
	}

	def "can create log content from String"() {
		expect:
		of('foo') instanceof CommandLineToolLogContentImpl
	}

	def "can get the exact log content as String"() {
		expect:
		of('foo').asString == 'foo'
		of('foo\r\nbar').asString == 'foo\r\nbar'
		of('').asString == ''
		of('''foo
bar
		far
	''').asString == 'foo\nbar\n\t\tfar\n\t'
	}

	def "throws exception when using null parser"() {
		when:
		of('foo').parse(null)

		then:
		def ex = thrown NullPointerException
		ex.message == 'Command line tool output parser cannot be null.'
	}

	def "can normalize line endings"() {
		expect:
		of('foo\r\nbar').withNormalizedEndOfLine() == of('foo\nbar')
		of('foo\r\n\r\nbar\r\n').withNormalizedEndOfLine() == of('foo\n\nbar\n')
		of('foo\r\r\n\r\nbar\r\n').withNormalizedEndOfLine() == of('foo\n\nbar\n')
		of('fo\ro\r\n\r\nbar\r\n').withNormalizedEndOfLine() == of('fo\ro\n\nbar\n')
		of('foo\nbar\n').withNormalizedEndOfLine() == of('foo\nbar\n')
	}

	def "removes whitespace at the end of line when normalizing end of lines"() {
		expect:
		of('foo \nbar').withNormalizedEndOfLine() == of('foo\nbar')
		of('foo\t\nbar').withNormalizedEndOfLine() == of('foo\nbar')
		of('foo\nbar ').withNormalizedEndOfLine() == of('foo\nbar')
		of('foo\nbar\t').withNormalizedEndOfLine() == of('foo\nbar')
		of('\t').withNormalizedEndOfLine() == empty()
		of('  ').withNormalizedEndOfLine() == empty()
	}

	def "can lines from the content"() {
		expect:
		of('foo\nbar').drop(1) == of('bar')
		of('foo\r\nbar').drop(1) == of('bar')
		of('foo\nbar').drop(2) == of('')
		of('foo\nbar').drop(5) == of('')

		and:
		of('aaa\r\nbbb\nccc\r\nddd').drop(1) == of('bbb\nccc\r\nddd')

		and:
		of('aaa\raaa\nbbb\nccc\r\nddd').drop(1) == of('bbb\nccc\r\nddd')

		and:
		of('\naaa\nbbb').drop(1) == of('aaa\nbbb')
		of('\n\naaa\nbbb').drop(1) == of('\naaa\nbbb')
		of('\r\naaa\r\nbbb').drop(1) == of('aaa\r\nbbb')
		of('\r\n\r\naaa\r\nbbb').drop(1) == of('\r\naaa\r\nbbb')

		and:
		of('\n\naaa\nbbb').drop(2) == of('aaa\nbbb')
		of('\r\n\r\naaa\r\nbbb').drop(2) == of('aaa\r\nbbb')
	}

	def "can interpret ANSI control characters"() {
		systemInstall()

		expect:
		of(ansi().eraseScreen().fg(RED).a("Hello").fg(GREEN).a(" World").reset().toString()).withAnsiControlCharactersInterpreted() == of('Hello World')
		of(ansi().eraseScreen().fg(RED).a("Hello").cursorDown(1).fg(GREEN).a(" World").reset().toString()).withAnsiControlCharactersInterpreted() == of('Hello\n      World')

		cleanup:
		systemUninstall()
	}

	def "can get the lines of the content"() {
		expect:
		of('foo\nbar').lines == ['foo', 'bar']
		of('\nfoo\nbar').lines == ['', 'foo', 'bar']
		of('foo\nbar\n\n').lines == ['foo', 'bar', '', '']

		and:
		of('foo\r\nbar').lines == ['foo', 'bar']
		of('\r\nfoo\r\nbar').lines == ['', 'foo', 'bar']
		of('foo\r\nbar\r\n\r\n').lines == ['foo', 'bar', '', '']

		and:
		of('  ').lines == ['  ']
	}
	//endregion

	//region Line visitor
	def "can visit multiple lines with Windows line ending"() {
		given:
		def visitor = Mock(Consumer)

		when:
		of('foo\r\nbar\r\nyolo').visitEachLine(visitor)

		then:
		1 * visitor.accept({ it.line == 'foo' })
		and:
		1 * visitor.accept({ it.line == 'bar' })
		and:
		1 * visitor.accept({ it.line == 'yolo' })
	}

	def "can visit multiple lines with *nix line ending"() {
		given:
		def visitor = Mock(Consumer)

		when:
		of('foo\nbar\nyolo').visitEachLine(visitor)

		then:
		1 * visitor.accept({ it.line == 'foo' })
		and:
		1 * visitor.accept({ it.line == 'bar' })
		and:
		1 * visitor.accept({ it.line == 'yolo' })
	}

	def "can visit multiple empty lines"() {
		given:
		def visitor = Mock(Consumer)

		when:
		of('\n\n').visitEachLine(visitor)
		then:
		3 * visitor.accept({ it.line == '' })

		when:
		of('\r\n\r\n').visitEachLine(visitor)
		then:
		3 * visitor.accept({ it.line == '' })
	}

	def "can visit lines of empty log"() {
		given:
		def visitor = Mock(Consumer)

		when:
		empty().visitEachLine(visitor)

		then:
		0 * visitor.accept(_)
	}

	def "do nothing line visitor return the same log"() {
		expect:
		of('foo\r\nbar\r\nyolo').visitEachLine(Stub(Consumer)) == of('foo\r\nbar\r\nyolo')
		of('foo\nbar\nyolo').visitEachLine(Stub(Consumer)) == of('foo\nbar\nyolo')
	}

	def "can drop a single line"() {
		given:
		def visitor = Stub(Consumer) {
			accept({ it.line == 'bar'}) >> { args -> args[0].dropLine() }
		}

		expect:
		of('foo\r\nbar\r\nyolo').visitEachLine(visitor) == of('foo\r\nyolo')
		of('foo\nbar\nyolo').visitEachLine(visitor) == of('foo\nyolo')
	}

	def "can drop multiple line until the end"() {
		given:
		def visitor = Stub(Consumer) {
			accept({ it.line == 'bar'}) >> { args -> args[0].drop(2) }
		}

		expect:
		of('foo\r\nbar\r\nyolo').visitEachLine(visitor) == of('foo')
		of('foo\nbar\nyolo').visitEachLine(visitor) == of('foo')
	}

	def "can drop multiple line from the middle"() {
		given:
		def visitor = Stub(Consumer) {
			accept({ it.line == 'bar'}) >> { args -> args[0].drop(2) }
		}

		expect:
		of('foo\r\nbar\r\nfar\r\nyolo').visitEachLine(visitor) == of('foo\r\nyolo')
		of('foo\nbar\nfar\nyolo').visitEachLine(visitor) == of('foo\nyolo')

		and: 'with mixed line separators'
		of('foo\r\nbar\r\nfar\nyolo').visitEachLine(visitor) == of('foo\r\nyolo')
		of('foo\nbar\r\nfar\r\nyolo').visitEachLine(visitor) == of('foo\nyolo')
	}

	def "can drop last line"() {
		given:
		def visitor = Stub(Consumer) {
			accept({ it.line == 'yolo'}) >> { args -> args[0].dropLine() }
		}

		expect:
		of('foo\r\nbar\r\nyolo').visitEachLine(visitor) == of('foo\r\nbar')
		of('foo\nbar\nyolo').visitEachLine(visitor) == of('foo\nbar')
	}

	def "can visit whitespace line"() {
		given:
		def visitor = Mock(Consumer)

		when:
		of('  ').visitEachLine(visitor)

		then:
		1 * visitor.accept({ it.line == '  ' })
	}

	def "can drop lines until empty"() {
		expect:
		of('foo\n\nbar\nyolo').visitEachLine({ it.dropLine() }) == empty()
		of('foo\n\nbar\nyolo').visitEachLine({ it.drop(42) }) == empty()
	}
	//endregion

	//region empty log content
	def "can create empty log content"() {
		expect:
		empty().asString == ''
	}

	def "can create empty log content from string"() {
		expect:
		CommandLineToolLogContent.of('') instanceof CommandLineToolLogContentEmptyImpl
	}

	def "returns same instance when dropping any number of lines"() {
		def subject = empty()

		expect:
		subject.drop(1) == subject
		subject.drop(4) == subject
		subject.drop(42) == subject
	}

	def "returns same instance when interpreting ANSI characters"() {
		def subject = empty()

		expect:
		subject.withAnsiControlCharactersInterpreted() == subject
	}

	def "returns same instance when normalizing line endings"() {
		def subject = empty()

		expect:
		subject.withNormalizedEndOfLine() == subject
	}

	def "can parse empty log content"() {
		def parser = Mock(CommandLineToolOutputParser)
		def obj = new Object()

		when:
		def result = empty().parse(parser)

		then:
		1 * parser.parse('') >> obj
		result == obj
	}

	def "returns empty list of lines"() {
		expect:
		empty().lines == []
	}
	//endregion
}
