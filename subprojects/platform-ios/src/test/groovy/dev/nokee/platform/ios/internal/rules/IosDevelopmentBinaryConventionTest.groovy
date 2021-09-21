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
package dev.nokee.platform.ios.internal.rules

import dev.nokee.platform.base.Binary
import dev.nokee.platform.ios.internal.SignedIosApplicationBundle
import dev.nokee.utils.ProviderUtils
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention.INSTANCE

@Subject(IosDevelopmentBinaryConvention)
class IosDevelopmentBinaryConventionTest extends Specification {
	def "returns undefined provider on empty list"() {
		expect:
		INSTANCE.transform([]) == ProviderUtils.notDefined()
	}

	def "throws exception when multiple SignedIosApplicationBundle binaries"() {
		when:
		INSTANCE.transform([Stub(SignedIosApplicationBundle), Stub(SignedIosApplicationBundle)])

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "expected one element but was: <Mock for type 'SignedIosApplicationBundle', Mock for type 'SignedIosApplicationBundle'>"
	}

	def "returns a provider of the single SignedIosApplicationBundle binary list"() {
		given:
		def binary = Stub(SignedIosApplicationBundle)

		expect:
		def result = INSTANCE.transform([binary])
		result.present
		result.get() == binary
	}

	def "returns a provider of a multi-binary list containing one SignedIosApplicationBundle binary"() {
		given:
		def binary = Stub(SignedIosApplicationBundle)

		expect:
		def result1 = INSTANCE.transform([binary, Stub(Binary)])
		result1.present
		result1.get() == binary

		and:
		def result2 = INSTANCE.transform([Stub(Binary), binary])
		result2.present
		result2.get() == binary

		and:
		def result3 = INSTANCE.transform([Stub(Binary), binary, Stub(Binary)])
		result3.present
		result3.get() == binary
	}
}
