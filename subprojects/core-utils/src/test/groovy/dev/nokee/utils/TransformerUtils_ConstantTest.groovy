package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TransformerUtils.constant

@Subject(TransformerUtils)
class TransformerUtils_ConstantTest extends Specification {
	def "ignores the input value"() {
		given:
		def obj1 = ['a', 'b', 'c']
		def obj2 = ['a', 'b', 'c'] as Set
		def obj3 = new Double(4.2)
		def obj4 = 'obj'

		expect:
		constant(42).transform(obj1) == 42
		constant(42).transform(obj2) == 42
		constant(42).transform(obj3) == 42
		constant(42).transform(obj4) == 42
	}

	def "returns the constant value"() {
		expect:
		constant(42).transform('dummy') == 42
		constant('42').transform('dummy') == '42'
		constant(['42']).transform('dummy') == ['42']
	}

	def "transformer toString() explains where the transformer comes from"() {
		expect:
		constant(42).toString() == 'TransformerUtils.constant(42)'
	}
}
