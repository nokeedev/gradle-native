package dev.nokee.platform.base.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import spock.lang.Specification
import spock.lang.Subject

@Subject(ComponentModelBasePlugin)
class ComponentModelBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies language base plugin"() {
		when:
		project.apply plugin: ComponentModelBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
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
