package dev.nokee.language.base.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(UTTypeUtils)
class UTTypeUtilsTest extends Specification {
	def "can create PatternFilterable compatible filter from UTType"() {
		def typeWithOneExtension = UTTypes.of('foo.bar', ['foo'] as String[])
		def typeWithMultipleExtensions = UTTypes.of('foo.bar', ['foo', 'bar'] as String[])
		def typeWithNoExtension = UTTypes.of('foo.bar', [] as String[])

		expect:
		UTTypeUtils.onlyIf(typeWithNoExtension).includes == [] as Set
		UTTypeUtils.onlyIf(typeWithOneExtension).includes == ['**/*.foo'] as Set
		UTTypeUtils.onlyIf(typeWithMultipleExtensions).includes == ['**/*.foo', '**/*.bar'] as Set
	}

	def "can create list of include compatible filter from UTType"() {
		def typeWithOneExtension = UTTypes.of('foo.bar', ['foo'] as String[])
		def typeWithMultipleExtensions = UTTypes.of('foo.bar', ['foo', 'bar'] as String[])
		def typeWithNoExtension = UTTypes.of('foo.bar', [] as String[])

		expect:
		UTTypeUtils.asFilenamePattern(typeWithNoExtension) == []
		UTTypeUtils.asFilenamePattern(typeWithOneExtension) == ['**/*.foo']
		UTTypeUtils.asFilenamePattern(typeWithMultipleExtensions) == ['**/*.foo', '**/*.bar']
	}
}
