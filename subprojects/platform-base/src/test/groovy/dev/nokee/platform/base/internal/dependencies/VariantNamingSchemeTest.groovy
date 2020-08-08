package dev.nokee.platform.base.internal.dependencies

import spock.lang.Specification
import spock.lang.Subject

@Subject(VariantNamingScheme) // only testing happy cases
class VariantNamingSchemeTest extends Specification {
	def "does not prefix when no dimension"() {
		expect:
		VariantNamingScheme.of().prefix('foo') == 'foo'
		VariantNamingScheme.of([]).prefix('foo') == 'foo'
	}

	def "prefixes with capitalized aggregation of the dimensions and target"() {
		expect:
		VariantNamingScheme.of('macos', 'x64').prefix('foo') == 'macosX64Foo'
	}
}
