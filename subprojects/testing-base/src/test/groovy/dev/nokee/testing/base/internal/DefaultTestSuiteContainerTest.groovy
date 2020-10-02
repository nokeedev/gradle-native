package dev.nokee.testing.base.internal

import dev.nokee.platform.base.internal.DefaultDomainObjectStore
import dev.nokee.testing.base.TestSuiteComponent
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DefaultTestSuiteContainerTest extends Specification {
	def objects = ProjectBuilder.builder().build().objects
	def store = objects.newInstance(DefaultDomainObjectStore, )
	def containerUnderTest

	def setup() {
		containerUnderTest = objects.newInstance(DefaultTestSuiteContainer, store)
		containerUnderTest.registerFactory(Foo, Foo, new NamedDomainObjectFactory<Foo>() {
			@Override
			Foo create(String name) {
				return new Foo(name)
			}
		})
	}

	def "calls when element known immediately"() {
		def c = []

		when:
		containerUnderTest.whenElementKnown { c << ':a' }
		then:
		c == []

		when:
		def provider = containerUnderTest.register('foo', Foo)
		then:
		c == [':a']

		when:
		containerUnderTest.whenElementKnown { c << ':b' }
		then:
		c == [':a', ':b']

		when:
		provider.get()
		then:
		c == [':a', ':b']
	}

	def "calls configure each only when realized"() {
		def c = []

		when:
		containerUnderTest.configureEach { c << ':a' }
		then:
		c == []

		when:
		def provider = containerUnderTest.register('foo', Foo)
		then:
		c == []

		when:
		containerUnderTest.configureEach { c << ':b' }
		then:
		c == []

		when:
		provider.get()
		then:
		c == [':a', ':b']
	}

	def "calls configuration action in order"() {
		def c = []

		given:
		containerUnderTest.whenElementKnown {
			it.configure { c << ':a' } // This gets added only when `foo` is registered
			c << ':b'
		}
		containerUnderTest.configureEach { c << ':c' }
		def provider = containerUnderTest.register('foo', Foo) { c << ':d' }
		containerUnderTest.configureEach { c << ':e' }
		containerUnderTest.whenElementKnown {
			c << ':f'
			it.configure { c << ':g' } // This gets added here as `foo` is already registered hence known
		}
		provider.configure { c << ':h' }

		when:
		provider.get()

		then:
		c == [':b', ':f', ':a', ':c', ':d', ':e', ':g', ':h']
	}

	def "can use Groovy decoration"() {
		def c = []

		given:
		containerUnderTest.configureEach { c << ':a' }
		containerUnderTest.whenElementKnown { c << ':b' }

		when:
		containerUnderTest.foo(Foo).get()

		then:
		c == [':b', ':a']
	}

	static class Foo implements TestSuiteComponent {
		private final String name

		Foo(String name) {
			this.name = name
		}

		@Override
		TestSuiteComponent testedComponent(Object component) {
			return null
		}
	}
}
