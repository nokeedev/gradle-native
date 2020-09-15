package dev.nokee.utils

import org.gradle.api.specs.Specs
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ProviderUtils.filter
import static dev.nokee.utils.TransformerUtils.constant
import static dev.nokee.utils.TransformerUtils.toListTransformer
import static java.util.Collections.emptyList

@Subject(ProviderUtils)
class ProviderUtils_FilterTest extends Specification {
	def "removes any elements not matching spec"() {
		given:
		def subject = filter({ it != 'foo' })

		expect:
		subject.transform(['bar', 'foo', 'far']) == ['bar', 'far']
	}

	def "can filter set"() {
		given:
		def subject = filter({ it != 'foo' })

		expect:
		subject.transform(['bar', 'foo', 'far'] as Set) == ['bar', 'far']
	}

	def "returns empty list for empty input set"() {
		given:
		def subject = filter({ true })

		expect:
		subject.transform([]) == []
	}

	def "transformer toString() explains where it comes from"() {
		given:
		def spec = {true}
		expect:
		filter(spec).toString() == "ProviderUtils.filter(${spec.toString()})"
	}

	def "returns a toListTransformer() for obvious satisfy all spec"() {
		expect:
		filter(Specs.satisfyAll()) == toListTransformer()
	}

	def "returns a constant(emptyList()) for obvious satisfy none spec"() {
		expect:
		filter(Specs.satisfyNone()) == constant(emptyList())
	}
}
