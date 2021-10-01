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

import dev.nokee.internal.testing.util.ProjectTestUtils
import spock.lang.Shared
import spock.lang.Specification

import static dev.nokee.utils.ConfigureUtils.setPropertyValue

class ConfigureUtils_SetPropertyValueTest extends Specification {
	@Shared def project = ProjectTestUtils.rootProject()

	def "can configure property"() {
		given:
		def property = project.objects.property(String)

		when:
		setPropertyValue(property, 'foo')
		then:
		property.get() == 'foo'

		when:
		setPropertyValue(property, project.provider { 'bar' })
		then:
		property.get() == 'bar'
	}

	def "can configure set property"() {
		given:
		def property = project.objects.setProperty(String)

		when:
		setPropertyValue(property, ['a', 'b', 'c'] as Set)
		then:
		property.get() == ['a', 'b', 'c'] as Set

		when:
		setPropertyValue(property, project.provider { ['x', 'y', 'z'] as Set })
		then:
		property.get() == ['x', 'y', 'z'] as Set
	}

	def "can configure list property"() {
		given:
		def property = project.objects.listProperty(String)

		when:
		setPropertyValue(property, ['a', 'b', 'c'])
		then:
		property.get() == ['a', 'b', 'c']

		when:
		setPropertyValue(property, project.provider { ['x', 'y', 'z'] })
		then:
		property.get() == ['x', 'y', 'z']
	}
}
