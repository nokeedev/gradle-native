package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TransformerUtils.noOpTransformer

@Subject(TransformerUtils)
class TransformerUtils_NoOpTransformerTest extends Specification {
	def "always return the same object"() {
		given:
		def obj1 = new Object()
		def obj2 = 'obj'
		def obj3 = new Double(4.2)

		expect:
		noOpTransformer().transform(obj1) == obj1
		noOpTransformer().transform(obj2) == obj2
		noOpTransformer().transform(obj3) == obj3
	}

	def "always the same instance"() {
		expect:
		noOpTransformer() == noOpTransformer()
	}

	def "transformer toString() explains where the transformer comes from"() {
		expect:
		noOpTransformer().toString() == 'TransformerUtils.noOpTransformer()'
	}
}
