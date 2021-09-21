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

@Subject(BinaryName)
class BinaryNameTest extends Specification {
	def "can create binary name"() {
		expect:
		BinaryName.of('executable').get() == 'executable'
		BinaryName.of('sharedLibrary').get() == 'sharedLibrary'
		BinaryName.of('staticLibrary').get() == 'staticLibrary'
	}

	def "can compare binary names"() {
		expect:
		BinaryName.of('foo') == BinaryName.of('foo')
		BinaryName.of('foo') != BinaryName.of('bar')
	}

	def "throws exception when name is null"() {
		when:
		BinaryName.of(null)

		then:
		thrown(NullPointerException)
	}

	def "throws exception when name is empty"() {
		when:
		BinaryName.of('')

		then:
		thrown(IllegalArgumentException)
	}

	def "throws exception when name starts with capital letter"() {
		when:
		BinaryName.of('Foo')

		then:
		thrown(IllegalArgumentException)
	}

	def "throws exception when name contains space"() {
		when:
		BinaryName.of('foo bar')

		then:
		thrown(IllegalArgumentException)
	}

	def "returns name value via toString()"() {
		expect:
		BinaryName.of('main').toString() == 'main'
		BinaryName.of('foo').toString() == 'foo'
		BinaryName.of('test').toString() == 'test'
	}
}
