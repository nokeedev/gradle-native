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

import dev.nokee.utils.ProviderUtils
import org.gradle.api.Transformer
import spock.lang.Specification

abstract class Value_AbstractTest extends Specification {
	abstract <T> Value<T> newSubject(T value)

	def "can create value"() {
		expect:
		newSubject(42) != null
	}

	def "can get the value"() {
		expect:
		newSubject(42).get() == 42
		newSubject(['a', 'b', 'c']).get() == ['a', 'b', 'c']
		newSubject('foo').get() == 'foo'
	}

	def "can get the type of the value"() {
		expect:
		newSubject(42).getType() == Integer
		newSubject(['a', 'b', 'c']).getType() == ArrayList
		newSubject('foo').getType() == String
		newSubject(new Foo(value: 42)).getType() == Foo
	}

	def "can convert to Gradle provider"() {
		expect:
		newSubject(42).toProvider() != null
		newSubject(42).toProvider().get() == 42
	}

	def "always return the same Gradle provider instance"() {
		given:
		def subject = newSubject(42)

		expect:
		subject.toProvider() == subject.toProvider()
	}

	def "returns provider when mapping value"() {
		expect:
		newSubject(42).map(Stub(Transformer)) != null
	}

	def "mapper function only called when getting the provider"() {
		given:
		def mapper = Mock(Transformer)

		when:
		def result = newSubject(42).map(mapper)
		then:
		0 * mapper.transform(_)

		when:
		result.get()
		then:
		1 * mapper.transform(42) >> 'foo'
		0 * mapper._
	}

	def "returns provider when flat mapping value"() {
		expect:
		newSubject(42).flatMap(Stub(Transformer)) != null
	}

	def "flat mapper function only called when getting the provider"() {
		given:
		def mapper = Mock(Transformer)

		when:
		def result = newSubject(42).flatMap(mapper)
		then:
		0 * mapper.transform(_)

		when:
		result.get()
		then:
		1 * mapper.transform(42) >> ProviderUtils.fixed('foo')
		0 * mapper._
	}

	def "returns value when mapping in place value"() {
		expect:
		newSubject(42).mapInPlace(Stub(Transformer)) != null
	}

	def "returns the same value instance when mapping in place value"() {
		given:
		def subject = newSubject(42)

		expect:
		subject.mapInPlace(Stub(Transformer)) == subject
	}

	def "can map in place value"() {
		expect:
		// Mutating the object itself as mapInPlace is approximated for Gradle provider
		newSubject(new Foo(value: 40)).mapInPlace({ it.value += 2 }).get().value == 42
	}

	def "can apply several in-place mapper value"() {
		expect:
		// Mutating the object itself as mapInPlace is approximated for Gradle provider
		newSubject(new Foo(value: 20)).mapInPlace({ it.value *= 2; it; }).mapInPlace({ it.value += 2; it; }).get().value == 42
	}

	def "in-place mapper are only called once"() {
		given:
		def mapper = Mock(Transformer)
		def subject = newSubject(42)

		when:
		subject.mapInPlace(mapper).mapInPlace(mapper).mapInPlace(mapper).get()
		then:
		3 * mapper.transform(_)

		when:
		subject.get()
		then:
		0 * mapper.transform(_)
	}

	def "in-place mapped value is conserved"() {
		given:
		def subject = newSubject(new Foo(value: 40)).mapInPlace({ it.value += 2 })

		expect:
		subject.get().value == 42
		subject.get().value == 42
	}

	def "in-place mapper are applied before map transform"() {
		given:
		def subject = newSubject(new Foo(value: 20))
		def mapper = Mock(Transformer)

		when:
		subject.mapInPlace({ it.value *= 2; it })
		def provider = subject.map(mapper)
		subject.mapInPlace({ it.value += 2; it })
		def result = provider.get()

		then:
		1 * mapper.transform({ it.value == 42 }) >> { args -> args[0].value }
		result == 42
	}

	static class Foo {
		int value
	}
}
