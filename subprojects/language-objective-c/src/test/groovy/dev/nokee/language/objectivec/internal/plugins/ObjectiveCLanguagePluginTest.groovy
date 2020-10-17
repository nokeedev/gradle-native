package dev.nokee.language.objectivec.internal.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCLanguagePlugin)
class ObjectiveCLanguagePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies Objective-C language base plugin"() {
		when:
		project.apply plugin: ObjectiveCLanguagePlugin

		then:
		project.plugins.hasPlugin(ObjectiveCLanguageBasePlugin)
	}
}
