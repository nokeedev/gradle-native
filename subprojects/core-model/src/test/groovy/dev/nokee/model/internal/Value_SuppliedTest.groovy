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
package dev.nokee.model.internal

import spock.lang.Subject

import java.util.function.Supplier

@Subject(Value)
class Value_SuppliedTest extends Value_AbstractTest {
	@Override
	def <T> Value<T> newSubject(T value) {
		return Value.supplied((Class<T>)value.getClass(), {value})
	}

	def "memoize the supplied value on get"() {
		given:
		def supplier = Mock(Supplier)
		def subject = Value.supplied(Integer, supplier)

		when:
		def result1 = subject.get()
		then:
		1 * supplier.get() >> 42
		result1 == 42

		when:
		def result2 = subject.get()
		then:
		0 * supplier.get()
		result2 == 42
	}

	def "querying the type does not resolve the supplier"() {
		given:
		def supplier = Mock(Supplier)
		def subject = Value.supplied(Object, supplier)

		when:
		subject.getType()

		then:
		0 * supplier.get()
	}
}
