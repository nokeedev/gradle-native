package dev.nokee.platform.base.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(BaseName)
class BaseNameTest extends Specification {
	def "can create base name"() {
		expect:
		BaseName.of('foo').toString() == 'foo'
		BaseName.of('foo-bar').toString() == 'foo-bar'
		BaseName.of('foo-bar_far').toString() == 'foo-bar_far'
		BaseName.of('foo_bar').toString() == 'foo_bar'
		BaseName.of('foo_bar-far').toString() == 'foo_bar-far'
		BaseName.of('fooBar').toString() == 'fooBar'
		BaseName.of('fooBar-far').toString() == 'fooBar-far'
		BaseName.of('fooBar_far').toString() == 'fooBar_far'
	}

	def "can get the base name as string"() {
		expect:
		BaseName.of('foo').asString == 'foo'
		BaseName.of('foo-bar').asString == 'foo-bar'
		BaseName.of('foo-bar_far').asString == 'foo-bar_far'
		BaseName.of('foo_bar').asString == 'foo_bar'
		BaseName.of('foo_bar-far').asString == 'foo_bar-far'
		BaseName.of('fooBar').asString == 'fooBar'
		BaseName.of('fooBar-far').asString == 'fooBar-far'
		BaseName.of('fooBar_far').asString == 'fooBar_far'
	}

	def "can get the base name as camel case"() {
		expect:
		BaseName.of('foo').asCamelCase == 'Foo'
		BaseName.of('foo-bar').asCamelCase == 'FooBar'
		BaseName.of('foo-bar_far').asCamelCase == 'FooBarfar'
		BaseName.of('foo_bar').asCamelCase == 'Foobar'
		BaseName.of('foo_bar-far').asCamelCase == 'FoobarFar'
		BaseName.of('fooBar').asCamelCase == 'FooBar'
		BaseName.of('fooBar-far').asCamelCase == 'FooBarFar'
		BaseName.of('fooBar_far').asCamelCase == 'FooBarfar'
	}

	def "can get the base name as kebab case"() {
		expect:
		BaseName.of('foo').asKebabCase == 'foo'
		BaseName.of('foo-bar').asKebabCase == 'foo-bar'
		BaseName.of('foo-bar_far').asKebabCase == 'foo-barfar'
		BaseName.of('foo_bar').asKebabCase == 'foobar'
		BaseName.of('foo_bar-far').asKebabCase == 'foobar-far'
		BaseName.of('fooBar').asKebabCase == 'foo-bar'
		BaseName.of('fooBar-far').asKebabCase == 'foo-bar-far'
		BaseName.of('fooBar_far').asKebabCase == 'foo-barfar'
	}

	def "can get the base name as lower camel case"() {
		expect:
		BaseName.of('foo').asLowerCamelCase == 'foo'
		BaseName.of('foo-bar').asLowerCamelCase == 'fooBar'
		BaseName.of('foo-bar_far').asLowerCamelCase == 'fooBarfar'
		BaseName.of('foo_bar').asLowerCamelCase == 'foobar'
		BaseName.of('foo_bar-far').asLowerCamelCase == 'foobarFar'
		BaseName.of('fooBar').asLowerCamelCase == 'fooBar'
		BaseName.of('fooBar-far').asLowerCamelCase == 'fooBarFar'
		BaseName.of('fooBar_far').asLowerCamelCase == 'fooBarfar'
	}

	def "can compare base names"() {
		expect:
		BaseName.of('foo') == BaseName.of('foo')
		BaseName.of('fooBar') == BaseName.of('fooBar')
		BaseName.of('foo-bar') == BaseName.of('foo-bar')
		BaseName.of('foo_bar') == BaseName.of('foo_bar')

		and:
		BaseName.of('foo') != BaseName.of('bar')
		BaseName.of('fooBar') != BaseName.of('foo-bar')
		BaseName.of('fooBar') != BaseName.of('foo_bar')
	}
}
