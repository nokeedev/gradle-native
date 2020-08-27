package dev.nokee.platform.base.internal.tasks

import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskName)
class TaskNameTest extends Specification {
	def "ensures task name is uncapitalized"() {
		when:
		TaskName.of('foo')
		then:
		noExceptionThrown()

		when:
		TaskName.of('Foo')
		then:
		thrown(AssertionError)
	}

	def "ensures verb and object is uncapitalized"() {
		when:
		TaskName.of('foo', 'bar')
		then:
		noExceptionThrown()

		when:
		TaskName.of('Foo', 'bar')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('Foo', 'Bar')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('foo', 'Bar')
		then:
		thrown(AssertionError)
	}

	def "name segments are not null"() {
		when:
		TaskName.of(null)
		then:
		thrown(AssertionError)

		when:
		TaskName.of(null, 'bar')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('foo', null)
		then:
		thrown(AssertionError)

		when:
		TaskName.of(null, null)
		then:
		thrown(AssertionError)
	}

	def "name segments are not empty"() {
		when:
		TaskName.of('')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('', 'bar')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('foo', '')
		then:
		thrown(AssertionError)

		when:
		TaskName.of('', '')
		then:
		thrown(AssertionError)
	}
}
