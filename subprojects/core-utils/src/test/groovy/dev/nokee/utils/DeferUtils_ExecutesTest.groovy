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
import spock.lang.Subject

import static dev.nokee.utils.DeferUtils.executes

@Subject(DeferUtils)
class DeferUtils_ExecutesTest extends Specification {
	def "does not run on creation"() {
		given:
		def runnable = Mock(Runnable)

		when:
		executes(runnable)

		then:
		0 * runnable.run()
	}

	def "run on resolving the provider"() {
		given:
		def runnable = Mock(Runnable)

		when:
		executes(runnable).getOrNull()

		then:
		1 * runnable.run()
	}

	def "returns empty list from provider"() {
		given:
		def runnable = Mock(Runnable)

		expect:
		executes(runnable).get() instanceof List
		executes(runnable).get().empty
	}
}
