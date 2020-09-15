package dev.nokee.platform.base.internal

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.ComponentName.of

@Subject(ComponentName)
class ComponentNameTest extends Specification {
	def "throws exception when name is null"() {
		when:
		of(null)

		then:
		thrown(IllegalArgumentException)
	}

	def "throws exception when name is empty"() {
		when:
		of('')

		then:
		thrown(IllegalArgumentException)
	}

	def "can create component name"() {
		expect:
		of('main').get() == 'main'
		of('foo').get() == 'foo'
		of('bar').get() == 'bar'
		of('Foo').get() == 'Foo'
		of('Bar').get() == 'Bar'
	}

	def "can compare names"() {
		expect:
		of('main') == of('main')
		of('foo') == of('foo')

		and:
		of('bar') != of('foo')

		and:
		of('Foo') == of('Foo')
		of('foo') != of('Foo')
	}
}
