package dev.nokee.platform.base.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(BinaryName)
class BinaryNameTest extends Specification {
	def "can create binary name"() {
		expect:
		BinaryName.of('executable').get() == 'executable'
		BinaryName.of('sharedLibrary').get() == 'sharedLibrary'
		BinaryName.of('staticLibrary').get() == 'staticLibrary'
	}

	def "can compare binary names"() {
		expect:
		BinaryName.of('foo') == BinaryName.of('foo')
		BinaryName.of('foo') != BinaryName.of('bar')
	}

	def "throws exception when name is null"() {
		when:
		BinaryName.of(null)

		then:
		thrown(NullPointerException)
	}

	def "throws exception when name is empty"() {
		when:
		BinaryName.of('')

		then:
		thrown(IllegalArgumentException)
	}

	def "throws exception when name starts with capital letter"() {
		when:
		BinaryName.of('Foo')

		then:
		thrown(IllegalArgumentException)
	}

	def "throws exception when name contains space"() {
		when:
		BinaryName.of('foo bar')

		then:
		thrown(IllegalArgumentException)
	}
}
