package dev.nokee.language.objectivecpp.internal.plugins


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCppLanguagePlugin)
class ObjectiveCppLanguagePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies Objective-C++ language base plugin"() {
		when:
		project.apply plugin: ObjectiveCppLanguagePlugin

		then:
		project.plugins.hasPlugin(ObjectiveCppLanguageBasePlugin)
	}
}
