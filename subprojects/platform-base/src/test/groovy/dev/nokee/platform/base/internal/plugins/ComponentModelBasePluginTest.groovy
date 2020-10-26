package dev.nokee.platform.base.internal.plugins

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ComponentModelBasePlugin)
class ComponentModelBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies language base plugin"() {
		when:
		project.apply plugin: ComponentModelBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}

	def "applies component base plugin"() {
		when:
		project.apply plugin: ComponentModelBasePlugin

		then:
		project.plugins.hasPlugin(ComponentBasePlugin)
	}

	def "applies binary base plugin"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.plugins.hasPlugin(BinaryBasePlugin)
	}

	def "applies task base plugin"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.plugins.hasPlugin(TaskBasePlugin)
	}

	def "applies variant base plugin"() {
		when:
		project.apply plugin: VariantBasePlugin

		then:
		project.plugins.hasPlugin(VariantBasePlugin)
	}
}
