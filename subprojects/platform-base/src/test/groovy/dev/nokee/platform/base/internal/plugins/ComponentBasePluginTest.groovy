package dev.nokee.platform.base.internal.plugins

import dev.nokee.platform.base.ComponentContainer
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ComponentBasePluginTest extends Specification {
	def "creates components container extension"() {
		given:
		def project = ProjectBuilder.builder().build()

		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.extensions.components instanceof ComponentContainer
		project.extensions.extensionsSchema.find { it.name == 'components' }.publicType.concreteClass == ComponentContainer
	}

	def "disallow changes after evaluation"() {
		given:
		def project = ProjectBuilder.builder().build()
		project.apply plugin: ComponentBasePlugin

		when:
		project.extensions.components.elements.get()
		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "applies lifecycle-base plugin"() {
		given:
		def project = ProjectBuilder.builder().build()

		and:
		assert !project.pluginManager.hasPlugin('lifecycle-base')

		when:
		project.apply plugin: ComponentBasePlugin

		then:
		project.pluginManager.hasPlugin('lifecycle-base')
	}
}
