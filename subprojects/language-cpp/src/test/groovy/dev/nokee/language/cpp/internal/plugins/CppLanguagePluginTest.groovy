package dev.nokee.language.cpp.internal.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(CppLanguagePlugin)
class CppLanguagePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies C++ language base plugin"() {
		when:
		project.apply plugin: CppLanguagePlugin

		then:
		project.plugins.hasPlugin(CppLanguageBasePlugin)
	}
}
