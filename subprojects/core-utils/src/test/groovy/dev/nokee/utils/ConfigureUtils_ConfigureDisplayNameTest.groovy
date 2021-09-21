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

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import spock.lang.Shared
import spock.lang.Specification

import static dev.nokee.utils.ConfigureUtils.configureDisplayName

class ConfigureUtils_ConfigureDisplayNameTest extends Specification {
	@Shared def project = ProjectTestUtils.rootProject()

	def "can configure display name of property"() {
		given:
		def property = project.objects.property(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of set property"() {
		given:
		def property = project.objects.setProperty(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of list property"() {
		given:
		def property = project.objects.listProperty(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of regular file property"() {
		given:
		def property = project.objects.fileProperty()

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of directory property"() {
		given:
		def property = project.objects.directoryProperty()

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "returns the property"() {
		given:
		def property = project.objects.property(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the set property"() {
		given:
		def property = project.objects.setProperty(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the list property"() {
		given:
		def property = project.objects.listProperty(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the regular file property"() {
		given:
		def property = project.objects.fileProperty()

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the directory property"() {
		given:
		def property = project.objects.directoryProperty()

		expect:
		configureDisplayName(property, 'foo') == property
	}
}
