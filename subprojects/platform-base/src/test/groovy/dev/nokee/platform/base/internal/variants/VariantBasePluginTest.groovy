package dev.nokee.platform.base.internal.variants

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.platform.base.internal.plugins.VariantBasePlugin
import spock.lang.Specification

class VariantBasePluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

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
