package dev.nokee.platform.base.internal

import spock.lang.Specification
import spock.lang.Unroll

class ComponentIdentifierTest extends Specification {
	def "can create identifier for main component"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = ComponentIdentifier.ofMain(ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name == 'main'
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
		def identifier = ComponentIdentifier.of(componentName, ownerIdentifier)

		then:
		!identifier.mainComponent
		identifier.name == componentName
		identifier.displayName == "component '${componentName}'"
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier

		where:
		componentName << ['test', 'integTest', 'uiTest']
	}

	def "can create identifier for main component by name"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = ComponentIdentifier.of('main', ownerIdentifier)

		then:
		identifier.mainComponent
		identifier.name == 'main'
		identifier.displayName == "main component"
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}

	def "main components are equal"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		ComponentIdentifier.of('main', ownerIdentifier) == ComponentIdentifier.ofMain(ownerIdentifier)
	}

	def "components with same name are equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		ComponentIdentifier.of('test', ownerIdentifier) == ComponentIdentifier.of('test', ownerIdentifier)
		ComponentIdentifier.of('integTest', ownerIdentifier) == ComponentIdentifier.of('integTest', ownerIdentifier)
	}

	def "components with different name are not equals"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		ComponentIdentifier.of('main', ownerIdentifier) != ComponentIdentifier.of('test', ownerIdentifier)
		ComponentIdentifier.of('test', ownerIdentifier) != ComponentIdentifier.of('integTest', ownerIdentifier)
	}

	def "components with owner are not equals"() {
		given:
		def ownerIdentifier1 = ProjectIdentifier.of('foo')
		def ownerIdentifier2 = ProjectIdentifier.of('bar')

		expect:
		ComponentIdentifier.of('main', ownerIdentifier1) != ComponentIdentifier.of('main', ownerIdentifier2)
		ComponentIdentifier.of('test', ownerIdentifier1) != ComponentIdentifier.of('test', ownerIdentifier2)
		ComponentIdentifier.ofMain(ownerIdentifier1) != ComponentIdentifier.ofMain(ownerIdentifier2)
	}

	def "can create identifier using the builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = ComponentIdentifier.builder().withName('main').withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name == 'main'
		identifier.mainComponent
		identifier.projectIdentifier == ownerIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == ownerIdentifier
	}


	def "can create identifier with custom display name using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		def identifier = ComponentIdentifier.builder().withName('main').withDisplayName('custom component').withProjectIdentifier(ownerIdentifier).build()

		then:
		identifier.name == 'main'
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
		ComponentIdentifier.builder().withName('main').withProjectIdentifier(ownerIdentifier).build().displayName == 'main component'
		ComponentIdentifier.builder().withName('test').withProjectIdentifier(ownerIdentifier).build().displayName == "component 'test'"
		ComponentIdentifier.builder().withName('integTest').withProjectIdentifier(ownerIdentifier).build().displayName == "component 'integTest'"
	}

	def "throws exceptions if name is null when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		ComponentIdentifier.of(null, ownerIdentifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because name is null.'
	}

	def "throws exceptions if name is empty when using factory method"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		ComponentIdentifier.of('', ownerIdentifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because name is invalid.'
	}

	def "throws exceptions if name is null when using constructor"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		new ComponentIdentifier(null, ownerIdentifier)
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because name is null.'

		when:
		new ComponentIdentifier(null, 'some display name', ownerIdentifier)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'Cannot construct a component identifier because name is null.'
	}

	def "throws exceptions if name is empty when using constructor"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		new ComponentIdentifier('', ownerIdentifier)
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == 'Cannot construct a component identifier because name is invalid.'

		when:
		new ComponentIdentifier('', 'some display name', ownerIdentifier)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'Cannot construct a component identifier because name is invalid.'
	}

	def "throws exceptions if name is null when using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		ComponentIdentifier.builder().withName(null).withProjectIdentifier(ownerIdentifier).build()
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because name is null.'
	}

	def "throws exceptions if name is empty when using builder"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		when:
		ComponentIdentifier.builder().withName('').withProjectIdentifier(ownerIdentifier).build()
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because name is invalid.'
	}

	def "throws exceptions if project identifier is null when using factory method"() {
		when:
		ComponentIdentifier.ofMain(null)
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == 'Cannot construct a component identifier because project identifier is null.'

		when:
		ComponentIdentifier.of('test', null)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'Cannot construct a component identifier because project identifier is null.'
	}

	def "throws exceptions if project identifier is null when using constructor"() {
		when:
		new ComponentIdentifier('test', null)
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == 'Cannot construct a component identifier because project identifier is null.'

		when:
		new ComponentIdentifier('test', 'some display name', null)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'Cannot construct a component identifier because project identifier is null.'
	}

	def "throws exceptions if project identifier is null when using builder"() {
		when:
		ComponentIdentifier.builder().withName('test').build()

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a component identifier because project identifier is null.'
	}

	def "uses default display name when using constructor"() {
		given:
		def ownerIdentifier = ProjectIdentifier.of('root')

		expect:
		new ComponentIdentifier('main', ownerIdentifier).displayName == 'main component'
		new ComponentIdentifier('test', ownerIdentifier).displayName == "component 'test'"
	}
}
