package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TransformerUtils.toSetTransformer

@Subject(TransformerUtils)
class TransformerUtils_ToSetTransformerTest extends Specification {
	def "always return the same object"() {
		given:
		def obj1 = ['a', 'b', 'c']
		def obj2 = ['a', 'b', 'c'] as Set

		expect:
		toSetTransformer().transform(obj1) == ['a', 'b', 'c'] as Set
		toSetTransformer().transform(obj2) == ['a', 'b', 'c'] as Set
	}

	def "always the same instance"() {
		expect:
		toSetTransformer() == toSetTransformer()
	}

	def "can cast each elements to specified type"() {
		given:
		Set<Object> obj = ['a', 'b', 'c']

		when:
		def result = toSetTransformer(String).transform(obj)

		then:
		noExceptionThrown()
		result == ['a', 'b', 'c'] as Set
	}

	def "transformer toString() explains where the transformer comes from"() {
		expect:
		toSetTransformer().toString() == 'TransformerUtils.toSetTransformer()'
	}
}
