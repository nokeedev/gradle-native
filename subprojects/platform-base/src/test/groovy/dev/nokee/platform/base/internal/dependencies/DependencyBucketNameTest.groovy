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
package dev.nokee.platform.base.internal.dependencies


import spock.lang.Specification
import spock.lang.Subject

@Subject(DependencyBucketName)
class DependencyBucketNameTest extends Specification {
	def "can create a bucket name using factory method"() {
		when:
		def name = DependencyBucketName.of('foo')

		then:
		noExceptionThrown()

		and:
		name.get() == 'foo'
	}

	def "buckets with the same name are equals"() {
		expect:
		DependencyBucketName.of('foo') == DependencyBucketName.of('foo')
		DependencyBucketName.of('bar') == DependencyBucketName.of('bar')
		DependencyBucketName.of('foo') != DependencyBucketName.of('bar')
	}

	def "throws an exception if name is null"() {
		when:
		DependencyBucketName.of(null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency bucket name because the name is null.'
	}

	def "throws an exception if name is empty"() {
		when:
		DependencyBucketName.of('')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency bucket name because the name is empty.'
	}

	def "throws an exception if name is capitalize"() {
		when:
		DependencyBucketName.of('Foo')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency bucket name because the name is capitalized.'
	}

	def "to string check"() {
		expect:
		DependencyBucketName.of("fooBar").toString() == 'fooBar'
	}
}
