package dev.nokee.language.base.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypes)
class UTTypesTest extends Specification {
	def "can create UTType with identifier and filename extensions"() {
		when:
		def type = UTTypes.of('foo.bar', ['foo'] as String[])

		then:
		noExceptionThrown()

		and:
		type != null
	}

	def "can query the identifier of a type"() {
		when:
		def type = UTTypes.of('foo.bar', ['foo'] as String[])

		then:
		type.identifier == 'foo.bar'
	}

	def "can query the filename extensions of a type"() {
		when:
		def type = UTTypes.of('foo.bar', ['foo'] as String[])

		then:
		type.filenameExtensions == ['foo'] as String[]
	}

	def "can compare type by identifier only"() {
		when:
		def type1 = UTTypes.of('foo.bar', ['foo'] as String[])
		def type2 = UTTypes.of('foo.bar', ['foo'] as String[])
		def type3 = UTTypes.of('foo.bar', [] as String[])
		def type4 = UTTypes.of('bar.foo', ['foo'] as String[])

		then:
		type1 == type2
		type1 == type3
		type1 != type4
	}

	def "throws exception if identifier is null"() {
		when:
		UTTypes.of(null, ['foo'] as String[])

		then:
		def ex = thrown(NullPointerException)
		ex.message == 'identifier is marked non-null but is null'
	}

	def "throws exception if filename extensions is null"() {
		when:
		UTTypes.of('foo.bar', null)

		then:
		def ex = thrown(NullPointerException)
		ex.message == 'filenameExtensions is marked non-null but is null'
	}

	def "has a sensible default display name"() {
		def type = UTTypes.of('foo.bar', ['foo'] as String[])
		expect:
		type.displayName == "Uniform type 'foo.bar'"
	}
}
