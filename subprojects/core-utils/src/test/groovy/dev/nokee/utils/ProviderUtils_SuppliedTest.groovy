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

import spock.lang.Specification

class ProviderUtils_SuppliedTest extends Specification {
	def "can create a supplied value Gradle provider"() {
		expect:
		ProviderUtils.supplied({42}).get() == 42
		ProviderUtils.supplied({['a', 'b', 'c']}).get() == ['a', 'b', 'c']
		ProviderUtils.supplied({'foo'}).get() == 'foo'
	}

	def "throws exception for null value"() {
		when:
		ProviderUtils.supplied(null)

		then:
		thrown(NullPointerException)
	}
}
