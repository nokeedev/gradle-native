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

import dev.nokee.model.internal.NameAwareDomainObjectIdentifier
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier
import dev.nokee.platform.base.Component
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static dev.nokee.platform.base.internal.ComponentIdentifier.*

class ComponentIdentifierTest extends Specification {
	def "can create identifier for main component"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = ofMain(ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name.get() == 'main'
		identifier.displayName == 'main component'
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}

	@Unroll
	def "can create identifier for non-main component"(componentName) {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = of(componentName, ownerIdentifier)

		then:
		!identifier.mainComponent
		identifier.name == componentName
		identifier.displayName == "component '${componentName.get()}'"
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier

		where:
		componentName << [ComponentName.of('test'), ComponentName.of('integTest'), ComponentName.of('uiTest')]
	}

	def "can create identifier for main component by name"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = of(ComponentName.of('main'), ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name.get() == 'main'
		identifier.displayName == "main component"
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}

	def "main components are equal based on types"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('main'), ownerIdentifier) == ofMain(ownerIdentifier)
		of(ComponentName.of('main'), ownerIdentifier) != ofMain(ownerIdentifier)
	}

	def "components with same name are equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('test'), ownerIdentifier) == of(ComponentName.of('test'), ownerIdentifier)
		of(ComponentName.of('test'), ownerIdentifier) != of(ComponentName.of('test'), ownerIdentifier)
		of(ComponentName.of('integTest'), ownerIdentifier) == of(ComponentName.of('integTest'), ownerIdentifier)
		of(ComponentName.of('integTest'), ownerIdentifier) != of(ComponentName.of('integTest'), ownerIdentifier)
	}

	def "components with different name are not equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('main'), ownerIdentifier) != of(ComponentName.of('test'), ownerIdentifier)
		of(ComponentName.of('test'), ownerIdentifier) != of(ComponentName.of('integTest'), ownerIdentifier)
	}

	def "components with owner are not equals"() {
		given:
		def ownerIdentifier1 = ProjectIdentifier.of('foo')
		def ownerIdentifier2 = ProjectIdentifier.of('bar')

		expect:
		of(ComponentName.of('main'), ownerIdentifier1) != of(ComponentName.of('main'), ownerIdentifier2)
		of(ComponentName.of('test'), ownerIdentifier1) != of(ComponentName.of('test'), ownerIdentifier2)
		ofMain(ownerIdentifier1) != ofMain(ownerIdentifier2)
	}

	def "can create identifier using the builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = builder().withName(ComponentName.of('main')).withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name.get() == 'main'
		identifier.mainComponent
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}


	def "can create identifier with custom display name using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = builder().withName(ComponentName.of('main')).withDisplayName('custom component').withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name.get() == 'main'
		identifier.mainComponent
		identifier.displayName == 'custom component'
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}

	def "use default display name when none is specified when using the builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		builder().withName(ComponentName.of('main')).withProjectIdentifier(ownerIdentifier).build().displayName == 'main component'
		builder().withName(ComponentName.of('test')).withProjectIdentifier(ownerIdentifier).build().displayName == "component 'test'"
		builder().withName(ComponentName.of('integTest')).withProjectIdentifier(ownerIdentifier).build().displayName == "component 'integTest'"
	}

	def "throws exceptions if name is null when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		of(null, ownerIdentifier)

		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if type is null when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		of(ComponentName.of('test'), ownerIdentifier)
		then:
		thrown(NullPointerException)

		when:
		ofMain(ownerIdentifier)
		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if name is null when using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		builder().withName(null).withProjectIdentifier(ownerIdentifier).build()

		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if project identifier is null when using factory method"() {
		when:
		ofMain(null)
		then:
		thrown(NullPointerException)

		when:
		of(ComponentName.of('test'), null)
		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if project identifier is null when using builder"() {
		when:
		builder().withName(ComponentName.of('test')).build()

		then:
		thrown(NullPointerException)
	}

	def "is name aware"() {
		expect:
		ofMain(ProjectIdentifier.of('foo')) instanceof NameAwareDomainObjectIdentifier
		of(ComponentName.of('test'), ProjectIdentifier.of('foo')) instanceof NameAwareDomainObjectIdentifier
	}

	def "has meaningful toString() implementation"() {
		given:
		def rootProject = ProjectBuilder.builder().withName('foo').build()
		def childProject = ProjectBuilder.builder().withName('bar').withParent(rootProject).build()

		expect:
		ofMain(ProjectIdentifier.of('foo')).toString() == "component ':main'"
		of(ComponentName.of('integTest'), ProjectIdentifier.of('bar')).toString() == "component ':integTest'"

		and:
		ofMain(ProjectIdentifier.of(rootProject)).toString() == "component ':main'"
		ofMain(ProjectIdentifier.of(childProject)).toString() == "component ':bar:main'"

		and:
		of(ComponentName.of('integTest'), ProjectIdentifier.of(rootProject)).toString() == "component ':integTest'"
		of(ComponentName.of('integTest'), ProjectIdentifier.of(childProject)).toString() == "component ':bar:integTest'"
	}
}
