package dev.nokee.platform.base.internal.variants


import dev.nokee.platform.base.internal.plugins.VariantBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class VariantBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers variant configurer service"() {
		when:
		project.apply plugin: VariantBasePlugin

		then:
		project.extensions.findByType(VariantConfigurer) != null
	}

	def "registers variant repository service"() {
		when:
		project.apply plugin: VariantBasePlugin

		then:
		project.extensions.findByType(VariantRepository) != null
	}

	def "registers variant view factory"() {
		when:
		project.apply plugin: VariantBasePlugin

		then:
		project.extensions.findByType(VariantViewFactory) != null
	}

	def "registers known variant factory"() {
		when:
		project.apply plugin: VariantBasePlugin

		then:
		project.extensions.findByType(KnownVariantFactory) != null
	}
}
