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
import dev.nokee.platform.base.Binary
import org.gradle.testfixtures.ProjectBuilder
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
		def result = BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		then:
		result.name.get() == 'foo'
		result.ownerIdentifier == variantIdentifier
	}

	def "can create identifier owned by component"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)

		when:
		def result = BinaryIdentifier.of(BinaryName.of('foo'), componentIdentifier)

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
		BinaryIdentifier.of(null, variantIdentifier)

		then:
		thrown(NullPointerException)
	}

	def "throws exception if type is null"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', componentIdentifier)

		when:
		BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a binary identifier because the task type is null.'
	}

	def "throws exception if owner is null"() {
		when:
		BinaryIdentifier.of(BinaryName.of('foo'), null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a task identifier because the owner identifier is null.'
	}

	def "has meaningful toString() implementation"() {
		given:
		def rootProject = ProjectBuilder.builder().withName('foo').build()
		def childProject = ProjectBuilder.builder().withName('bar').withParent(rootProject).build()

		and:
		def ownerProject = ProjectIdentifier.of(childProject)
		def ownerComponent = ComponentIdentifier.ofMain(ownerProject)
		def ownerVariant = VariantIdentifier.of('macosRelease', ownerComponent)

		expect:
		BinaryIdentifier.of(BinaryName.of('bar'), ownerComponent).toString() == "binary ':main:bar'"
		BinaryIdentifier.of(BinaryName.of('jar'), ownerVariant).toString() == "binary ':main:macosRelease:jar'"
	}

	interface TestableBinary extends Binary {}
}
