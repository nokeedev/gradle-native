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
package dev.nokee.utils.internal

import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(AssertingTaskAction)
class AssertingTaskActionTest extends Specification {
	def "calls expression supplier only once"() {
		given:
		def expression = Mock(Supplier)

		when:
		new AssertingTaskAction(expression, "Error message").execute(Mock(Task))

		then:
		1 * expression.get() >> true
	}

	def "can use supplier as error message for lazy error message"() {
		given:
		def expression = Mock(Supplier)
		def errorMessage = Mock(Supplier)

		when:
		new AssertingTaskAction(expression, errorMessage).execute(Mock(Task))
		then:
		1 * expression.get() >> true
		0 * errorMessage.get()

		when:
		new AssertingTaskAction(expression, errorMessage).execute(Mock(Task))
		then:
		1 * expression.get() >> false
		1 * errorMessage.get() >> 'Computed error message'
		and:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Computed error message'
	}
}
