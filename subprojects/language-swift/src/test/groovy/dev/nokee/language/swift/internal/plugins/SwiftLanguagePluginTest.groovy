package dev.nokee.language.swift.internal.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(SwiftLanguagePlugin.class)
class SwiftLanguagePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies base plugin"() {
		when:
		project.apply plugin: SwiftLanguagePlugin

		then:
		project.plugins.hasPlugin(SwiftLanguageBasePlugin)
	}
}
