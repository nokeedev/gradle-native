package dev.nokee.utils

import static dev.nokee.utils.DeferredUtils.flatUnpack
import static java.util.Collections.emptyList

class DeferredUtils_FlatUnpackTest extends DeferredUtils_BaseSpec {
	def "returns null when flat unpacking empty list"() {
		expect:
		flatUnpack(null) == []
	}

	def "can flat unpack already flatten and unpacked list"() {
		expect:
		flatUnpack(['a', 'b', 'c']) == ['a', 'b', 'c']
	}

	def "can flat an already unpacked list"() {
		expect:
		flatUnpack([['a', 'b'], ['c', ['d']]]) == ['a', 'b', 'c', 'd']
	}

	def "can unpack mixed single packed values"() {
		expect:
		flatUnpack([callableOf('a'), supplierOf('b'), closureOf('c'), kotlinFunctionOf('d')]) == ['a', 'b', 'c', 'd']
	}

	def "can unpack mixed multi-packed values"() {
		expect:
		flatUnpack([callableOf(['a1', 'a2']), supplierOf(['b1', 'b2']), closureOf(['c1', 'c2']), kotlinFunctionOf(['d1', 'd2'])]) == ['a1', 'a2', 'b1', 'b2', 'c1', 'c2', 'd1', 'd2']
	}

	def "can unpack list containing empty list"() {
		expect:
		flatUnpack([emptyList()]) == []
	}
}
