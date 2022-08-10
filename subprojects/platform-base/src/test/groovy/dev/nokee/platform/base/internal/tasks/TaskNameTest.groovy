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
package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.names.QualifyingName
import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskName)
class TaskNameTest extends Specification {
	def "can get task name verb"() {
		expect:
		TaskName.of('foo').verb == 'foo'
		TaskName.of('bar').verb == 'bar'
		TaskName.of('far').verb == 'far'
	}

	def "can get task name object when present"() {
		expect:
		TaskName.of('foo', 'c').object.get() == 'c'
		TaskName.of('foo', 'cpp').object.get() == 'cpp'
		TaskName.of('foo', 'swift').object.get() == 'swift'
	}

	def "object is absent when creating task name with verb only"() {
		expect:
		!TaskName.of('foo').object.present
		!TaskName.of('bar').object.present
		!TaskName.of('far').object.present
	}

	def "object is present when creating task name with verb and object"() {
		expect:
		TaskName.of('foo', 'c').object.present
		TaskName.of('bar', 'c').object.present
		TaskName.of('far', 'c').object.present
	}

	def "ensures task name is uncapitalized"() {
		when:
        TaskName.of('foo')
		then:
		noExceptionThrown()

		when:
        TaskName.of('Foo')
		then:
		thrown(IllegalArgumentException)
	}

	def "ensures verb and object is uncapitalized"() {
		when:
        TaskName.of('foo', 'bar')
		then:
		noExceptionThrown()

		when:
        TaskName.of('Foo', 'bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('Foo', 'Bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('foo', 'Bar')
		then:
		thrown(IllegalArgumentException)
	}

	def "name segments are not null"() {
		when:
        TaskName.of(null)
		then:
		thrown(NullPointerException)

		when:
        TaskName.of(null, 'bar')
		then:
		thrown(NullPointerException)

		when:
        TaskName.of('foo', null)
		then:
		thrown(NullPointerException)

		when:
        TaskName.of(null, null)
		then:
		thrown(NullPointerException)
	}

	def "name segments are not empty"() {
		when:
        TaskName.of('')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('', 'bar')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('foo', '')
		then:
		thrown(IllegalArgumentException)

		when:
        TaskName.of('', '')
		then:
		thrown(IllegalArgumentException)
	}

	def "can create empty task name"() {
		expect:
		TaskName.lifecycle().verb == ''
		!TaskName.lifecycle().object.present
	}

	def "can compare task name"() {
		expect:
		TaskName.lifecycle() == TaskName.lifecycle()
		TaskName.of('foo') == TaskName.of('foo')
		TaskName.of('foo', 'bar') == TaskName.of('foo', 'bar')

		and:
		TaskName.of('foo') != TaskName.of('bar')
		TaskName.of('foo') != TaskName.of('foo', 'bar')
		TaskName.of('foo') != TaskName.lifecycle()
	}

	def "can get task name"() {
		expect:
		TaskName.of('foo').get() == 'foo'
		TaskName.of('foo', 'bar').get() == 'fooBar'
		TaskName.lifecycle().get() == ''
	}

	def "returns name value via toString()"() {
		expect:
		TaskName.of('foo').toString() == 'foo'
		TaskName.of('foo', 'bar').toString() == 'fooBar'
		TaskName.lifecycle().toString() == ''
	}

	def "can create task name string using verb and object directly"() {
		expect:
		TaskName.taskName('foo', 'bar') == 'fooBar'
	}

	def "does not capitalize qualifying name on lifecycle tasks"() {
		expect:
		TaskName.lifecycle().toQualifiedName(QualifyingName.of('test')) == 'test'
		TaskName.lifecycle().toQualifiedName(QualifyingName.of('Test')) == 'Test'
	}
}
