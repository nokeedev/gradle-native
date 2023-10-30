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

import dev.nokee.model.internal.ProjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(BinaryIdentifier)
class BinaryIdentifierTest extends Specification {
	def "can create identifier owned by variant"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', componentIdentifier)

		when:
		def result = BinaryIdentifier.of(variantIdentifier, 'foo')

		then:
		result.name.get() == 'foo'
		result.ownerIdentifier == variantIdentifier
	}

	def "can create identifier owned by component"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)

		when:
		def result = BinaryIdentifier.of(componentIdentifier, 'foo')

		then:
		result.name.get() == 'foo'
		result.ownerIdentifier == componentIdentifier
	}

	def "throws exception if binary name is null"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', componentIdentifier)

		when:
		BinaryIdentifier.of(variantIdentifier, null)

		then:
		thrown(NullPointerException)
	}

	def "throws exception if owner is null"() {
		when:
		BinaryIdentifier.of(null, 'foo')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a task identifier because the owner identifier is null.'
	}
}
