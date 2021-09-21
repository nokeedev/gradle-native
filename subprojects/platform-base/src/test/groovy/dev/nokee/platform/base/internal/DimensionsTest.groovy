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

import spock.lang.Specification
import spock.lang.Subject

@Subject(Dimensions)
class DimensionsTest extends Specification {
	def "can create empty dimensions"() {
		expect:
		Dimensions.empty().get() == []
		Dimensions.empty().size() == 0
		!Dimensions.empty().asCamelCase.present
		!Dimensions.empty().asKebabCase.present
		!Dimensions.empty().asLowerCamelCase.present
	}

	def "can create empty dimensions from empty list"() {
		expect:
		Dimensions.of([]) == Dimensions.empty()
	}

	def "can create non-empty dimensions"() {
		when:
		def result = Dimensions.of(['foo', 'bar'])

		then:
		result.get() == ['foo', 'bar']
		result.size() == 2
		result.asCamelCase.present
		result.asKebabCase.present
		result.asLowerCamelCase.present
	}

	def "can add dimensions to empty"() {
		when:
		def result = Dimensions.empty().add('foo')

		then:
		result.get() == ['foo']
		result.size() == 1
		result.asCamelCase.present
		result.asKebabCase.present
		result.asLowerCamelCase.present
	}

	def "can add dimensions to non-empty"() {
		when:
		def result = Dimensions.of(['bar']).add('foo')

		then:
		result.size() == 2
		result.get() == ['bar', 'foo']
		result.asCamelCase.present
		result.asKebabCase.present
		result.asLowerCamelCase.present
	}

	def "can get dimensions as CamelCase"() {
		expect:
		Dimensions.of(['foo']).asCamelCase.get() == 'Foo'
		Dimensions.of(['foo', 'bar']).asCamelCase.get() == 'FooBar'
		Dimensions.of(['fooBar']).asCamelCase.get() == 'FooBar'
		Dimensions.of(['foo-bar', 'far']).asCamelCase.get() == 'Foo-barFar'
		Dimensions.of(['foo_bar', 'far']).asCamelCase.get() == 'Foo_barFar'
	}

	def "can get dimensions as lowerCamelCase"() {
		expect:
		Dimensions.of(['foo']).asLowerCamelCase.get() == 'foo'
		Dimensions.of(['foo', 'bar']).asLowerCamelCase.get() == 'fooBar'
		Dimensions.of(['fooBar']).asLowerCamelCase.get() == 'fooBar'
		Dimensions.of(['foo-bar', 'far']).asLowerCamelCase.get() == 'foo-barFar'
		Dimensions.of(['foo_bar', 'far']).asLowerCamelCase.get() == 'foo_barFar'
	}

	def "can get dimensions as kebab-case"() {
		expect:
		Dimensions.of(['foo']).asKebabCase.get() == 'foo'
		Dimensions.of(['foo', 'bar']).asKebabCase.get() == 'foo-bar'
		Dimensions.of(['fooBar']).asKebabCase.get() == 'fooBar'
		Dimensions.of(['foo-bar', 'far']).asKebabCase.get() == 'foo-bar-far'
		Dimensions.of(['foo_bar', 'far']).asKebabCase.get() == 'foo_bar-far'
	}
}
