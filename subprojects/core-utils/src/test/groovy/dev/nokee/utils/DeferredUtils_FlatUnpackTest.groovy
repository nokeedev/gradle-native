/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
