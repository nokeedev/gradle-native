/*
 * Copyright 2020-2021 the original author or authors.
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

import org.gradle.api.internal.provider.ProviderInternal
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

@Subject(ProviderUtils)
class ProviderUtils_GetTypeTest extends Specification {
	def "return type from ProviderInternal"() {
		given:
		def provider = Mock(ProviderInternal)

		when:
		def result = ProviderUtils.getType(provider)

		then:
		1 * provider.getType() >> Object
		result.get() == Object
	}

	def "can return null if ProviderInternal type is null"() {
		given:
		def provider = Stub(ProviderInternal) {
			getType() >> null
		}

		expect:
		!ProviderUtils.getType(provider).present
	}

	def "returns null if provider is not a ProviderInternal instance"() {
		given:
		def provider = Stub(Provider)

		expect:
		!ProviderUtils.getType(provider).present
	}

	def "throws exception is provider is null"() {
		when:
		ProviderUtils.getType(null)

		then:
		thrown(NullPointerException)
	}
}
