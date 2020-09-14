package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TransformerUtils.toListTransformer

@Subject(TransformerUtils)
class TransformerUtils_ToListTransformerTest extends Specification {
	def "always return the same object"() {
		given:
		def obj1 = ['a', 'b', 'c']
		def obj2 = ['a', 'b', 'c'] as Set

		expect:
		toListTransformer().transform(obj1) == ['a', 'b', 'c']
		toListTransformer().transform(obj2) == ['a', 'b', 'c']
	}

	def "always the same instance"() {
		expect:
		toListTransformer() == toListTransformer()
	}

	def "transformer toString() explains where the transformer comes from"() {
		expect:
		toListTransformer().toString() == 'TransformerUtils.toListTransformer()'
	}
}
