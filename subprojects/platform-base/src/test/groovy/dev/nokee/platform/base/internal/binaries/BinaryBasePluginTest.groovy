package dev.nokee.platform.base.internal.binaries

import dev.nokee.platform.base.internal.plugins.BinaryBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class BinaryBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers binary configurer service"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryConfigurer) != null
	}

	def "registers binary repository service"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryRepository) != null
	}

	def "registers binary view factory"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryViewFactory) != null
	}

	def "registers known binary factory"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(KnownBinaryFactory) != null
	}
}
