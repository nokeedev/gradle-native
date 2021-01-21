package dev.nokee.platform.base.internal.tasks

import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskName)
class TaskNameTest extends Specification {
	def "can get task name verb"() {
		expect:
		TaskName.of('foo').verb == 'foo'
		TaskName.of('bar').verb == 'bar'
		TaskName.of('far').verb == 'far'
	}

	def "can get task name object when present"() {
		expect:
		TaskName.of('foo', 'c').object.get() == 'c'
		TaskName.of('foo', 'cpp').object.get() == 'cpp'
		TaskName.of('foo', 'swift').object.get() == 'swift'
	}

	def "object is absent when creating task name with verb only"() {
		expect:
		!TaskName.of('foo').object.present
		!TaskName.of('bar').object.present
		!TaskName.of('far').object.present
	}

	def "object is present when creating task name with verb and object"() {
		expect:
		TaskName.of('foo', 'c').object.present
		TaskName.of('bar', 'c').object.present
		TaskName.of('far', 'c').object.present
	}

	def "ensures task name is uncapitalized"() {
		when:
        TaskName.of('foo')
		then:
		noExceptionThrown()

		when:
        TaskName.of('Foo')
		then:
		thrown(IllegalArgumentException)
	}

	def "ensures verb and object is uncapitalized"() {
		when:
        TaskName.of('foo', 'bar')
		then:
		noExceptionThrown()

		when:
        TaskName.of('Foo', 'bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('Foo', 'Bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('foo', 'Bar')
		then:
		thrown(IllegalArgumentException)
	}

	def "name segments are not null"() {
		when:
        TaskName.of(null)
		then:
		thrown(NullPointerException)

		when:
        TaskName.of(null, 'bar')
		then:
		thrown(NullPointerException)

		when:
        TaskName.of('foo', null)
		then:
		thrown(NullPointerException)

		when:
        TaskName.of(null, null)
		then:
		thrown(NullPointerException)
	}

	def "name segments are not empty"() {
		when:
        TaskName.of('')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('', 'bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('foo', '')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('', '')
		then:
		thrown(IllegalArgumentException)
	}

	def "can create empty task name"() {
		expect:
		TaskName.empty().verb == ''
		!TaskName.empty().object.present
	}

	def "can compare task name"() {
		expect:
		TaskName.empty() == TaskName.empty()
		TaskName.of('foo') == TaskName.of('foo')
		TaskName.of('foo', 'bar') == TaskName.of('foo', 'bar')

		and:
		TaskName.of('foo') != TaskName.of('bar')
		TaskName.of('foo') != TaskName.of('foo', 'bar')
		TaskName.of('foo') != TaskName.empty()
	}

	def "can get task name"() {
		expect:
		TaskName.of('foo').get() == 'foo'
		TaskName.of('foo', 'bar').get() == 'fooBar'
		TaskName.empty().get() == ''
	}

	def "returns name value via toString()"() {
		expect:
		TaskName.of('foo').toString() == 'foo'
		TaskName.of('foo', 'bar').toString() == 'fooBar'
		TaskName.empty().toString() == ''
	}

	def "can create task name string using verb and object directly"() {
		expect:
		TaskName.taskName('foo', 'bar') == 'fooBar'
	}
}
