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
		def identifier = ofMain(TestableComponent, ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name.get() == 'main'
		identifier.type == TestableComponent
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
		def identifier = of(componentName, TestableComponent, ownerIdentifier)

		then:
		!identifier.mainComponent
		identifier.name == componentName
		identifier.type == TestableComponent
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
		def identifier = of(ComponentName.of('main'), TestableComponent, ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name.get() == 'main'
		identifier.type == TestableComponent
		identifier.displayName == "main component"
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}

	def "main components are equal based on types"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('main'), TestableComponent, ownerIdentifier) == ofMain(TestableComponent, ownerIdentifier)
		of(ComponentName.of('main'), TestableComponent, ownerIdentifier) != ofMain(AnotherTestableComponent, ownerIdentifier)
	}

	def "components with same name are equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('test'), TestableComponent, ownerIdentifier) == of(ComponentName.of('test'), TestableComponent, ownerIdentifier)
		of(ComponentName.of('test'), TestableComponent, ownerIdentifier) != of(ComponentName.of('test'), AnotherTestableComponent, ownerIdentifier)
		of(ComponentName.of('integTest'), TestableComponent, ownerIdentifier) == of(ComponentName.of('integTest'), TestableComponent, ownerIdentifier)
		of(ComponentName.of('integTest'), TestableComponent, ownerIdentifier) != of(ComponentName.of('integTest'), AnotherTestableComponent, ownerIdentifier)
	}

	def "components with different name are not equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		of(ComponentName.of('main'), TestableComponent, ownerIdentifier) != of(ComponentName.of('test'), TestableComponent, ownerIdentifier)
		of(ComponentName.of('test'), TestableComponent, ownerIdentifier) != of(ComponentName.of('integTest'), TestableComponent, ownerIdentifier)
	}

	def "components with owner are not equals"() {
		given:
		def ownerIdentifier1 = ProjectIdentifier.of('foo')
		def ownerIdentifier2 = ProjectIdentifier.of('bar')

		expect:
		of(ComponentName.of('main'), TestableComponent, ownerIdentifier1) != of(ComponentName.of('main'), TestableComponent, ownerIdentifier2)
		of(ComponentName.of('test'), TestableComponent, ownerIdentifier1) != of(ComponentName.of('test'), TestableComponent, ownerIdentifier2)
		ofMain(TestableComponent, ownerIdentifier1) != ofMain(TestableComponent, ownerIdentifier2)
	}

	def "can create identifier using the builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = builder().withName(ComponentName.of('main')).withType(TestableComponent).withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name.get() == 'main'
		identifier.type == TestableComponent
		identifier.mainComponent
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}


	def "can create identifier with custom display name using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = builder().withName(ComponentName.of('main')).withType(TestableComponent).withDisplayName('custom component').withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name.get() == 'main'
		identifier.type == TestableComponent
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
		builder().withName(ComponentName.of('main')).withType(TestableComponent).withProjectIdentifier(ownerIdentifier).build().displayName == 'main component'
		builder().withName(ComponentName.of('test')).withType(TestableComponent).withProjectIdentifier(ownerIdentifier).build().displayName == "component 'test'"
		builder().withName(ComponentName.of('integTest')).withType(TestableComponent).withProjectIdentifier(ownerIdentifier).build().displayName == "component 'integTest'"
	}

	def "throws exceptions if name is null when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		of(null, TestableComponent, ownerIdentifier)

		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if type is null when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		of(ComponentName.of('test'), null, ownerIdentifier)
		then:
		thrown(NullPointerException)

		when:
		ofMain(null, ownerIdentifier)
		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if name is null when using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		builder().withName(null).withType(TestableComponent).withProjectIdentifier(ownerIdentifier).build()

		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if project identifier is null when using factory method"() {
		when:
		ofMain(TestableComponent, null)
		then:
		thrown(NullPointerException)

		when:
		of(ComponentName.of('test'), TestableComponent, null)
		then:
		thrown(NullPointerException)
	}

	def "throws exceptions if project identifier is null when using builder"() {
		when:
		builder().withName(ComponentName.of('test')).withType(TestableComponent).build()

		then:
		thrown(NullPointerException)
	}

	def "is type aware"() {
		expect:
		ofMain(TestableComponent, ProjectIdentifier.of('foo')) instanceof TypeAwareDomainObjectIdentifier
		of(ComponentName.of('test'), TestableComponent, ProjectIdentifier.of('foo')) instanceof TypeAwareDomainObjectIdentifier
	}

	def "is name aware"() {
		expect:
		ofMain(TestableComponent, ProjectIdentifier.of('foo')) instanceof NameAwareDomainObjectIdentifier
		of(ComponentName.of('test'), TestableComponent, ProjectIdentifier.of('foo')) instanceof NameAwareDomainObjectIdentifier
	}

	def "has meaningful toString() implementation"() {
		given:
		def rootProject = ProjectBuilder.builder().withName('foo').build()
		def childProject = ProjectBuilder.builder().withName('bar').withParent(rootProject).build()

		expect:
		ofMain(TestableComponent, ProjectIdentifier.of('foo')).toString() == "component ':main' (${TestableComponent.simpleName})"
		of(ComponentName.of('integTest'), TestableComponent, ProjectIdentifier.of('bar')).toString() == "component ':integTest' (${TestableComponent.simpleName})"

		and:
		ofMain(TestableComponent, ProjectIdentifier.of(rootProject)).toString() == "component ':main' (${TestableComponent.simpleName})"
		ofMain(TestableComponent, ProjectIdentifier.of(childProject)).toString() == "component ':bar:main' (${TestableComponent.simpleName})"

		and:
		of(ComponentName.of('integTest'), TestableComponent, ProjectIdentifier.of(rootProject)).toString() == "component ':integTest' (${TestableComponent.simpleName})"
		of(ComponentName.of('integTest'), TestableComponent, ProjectIdentifier.of(childProject)).toString() == "component ':bar:integTest' (${TestableComponent.simpleName})"
	}

	interface TestableComponent extends Component {}
	interface AnotherTestableComponent extends Component {}
}
