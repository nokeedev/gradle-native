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
package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Binary
import dev.nokee.utils.ProviderUtils
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.DevelopmentBinaryUtils.selectSingleBinaryByType

@Subject(DevelopmentBinaryUtils)
class DevelopmentBinaryUtils_SelectSingleBinaryByTypeTest extends Specification {
	def "returns undefined provider on empty list"() {
		expect:
		selectSingleBinaryByType(MyBinary, []) == ProviderUtils.notDefined()
	}

	def "throws exception when multiple MyBinary binaries"() {
		when:
		selectSingleBinaryByType(MyBinary, [Stub(MyBinary), Stub(MyBinary)])

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "expected one element but was: <Mock for type 'MyBinary', Mock for type 'MyBinary'>"
	}

	def "returns a provider of the single MyBinary binary list"() {
		given:
		def binary = Stub(MyBinary)

		expect:
		def result = selectSingleBinaryByType(MyBinary, [binary])
		result.present
		result.get() == binary
	}

	def "returns a provider of a multi-binary list containing one MyBinary binary"() {
		given:
		def binary = Stub(MyBinary)

		expect:
		def result1 = selectSingleBinaryByType(MyBinary, [binary, Stub(Binary)])
		result1.present
		result1.get() == binary

		and:
		def result2 = selectSingleBinaryByType(MyBinary, [Stub(Binary), binary])
		result2.present
		result2.get() == binary

		and:
		def result3 = selectSingleBinaryByType(MyBinary, [Stub(Binary), binary, Stub(Binary)])
		result3.present
		result3.get() == binary
	}

	interface MyBinary extends Binary {}
}
