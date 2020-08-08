package dev.nokee.platform.base.internal.dependencies

import spock.lang.Specification
import spock.lang.Subject

@Subject(ComponentNamingScheme)
class ComponentNamingSchemeTest extends Specification {
	def "does not prefix when component name is main"() {
		expect:
		ComponentNamingScheme.ofMain().prefix('foo') == 'foo'
		ComponentNamingScheme.of('main').prefix('foo') == 'foo'
	}

	def "prefixes with aggregation of the name and capitalized target"() {
		expect:
		ComponentNamingScheme.of('test').prefix('foo') == 'testFoo'
	}
}
