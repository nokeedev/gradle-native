package dev.nokee.platform.base.internal.components

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.platform.base.ComponentContainer
import dev.nokee.platform.base.internal.plugins.ComponentBasePlugin
import spock.lang.Specification

class ComponentBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "creates components container extension"() {
		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.components instanceof ComponentContainer
		project.extensions.extensionsSchema.find { it.name == 'components' }.publicType.concreteClass == ComponentContainer
	}

	def "disallow changes after evaluation"() {
		given:
		project.apply plugin: ComponentBasePlugin

		when:
		project.extensions.components.elements.get()
		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "applies lifecycle-base plugin"() {
		given:
		assert !project.pluginManager.hasPlugin('lifecycle-base')

		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.pluginManager.hasPlugin('lifecycle-base')
	}

	def "registers component configurer service"() {
		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.findByType(ComponentConfigurer) != null
	}

	def "registers component repository service"() {
		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.findByType(ComponentRepository) != null
	}

	def "registers known component factory"() {
		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.findByType(KnownComponentFactory) != null
	}

	def "registers component instantiator"() {
		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.findByType(ComponentInstantiator) != null
	}
}
