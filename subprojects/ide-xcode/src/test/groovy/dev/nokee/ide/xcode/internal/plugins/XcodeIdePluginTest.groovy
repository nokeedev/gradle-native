package dev.nokee.ide.xcode.internal.plugins


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(XcodeIdePlugin)
class XcodeIdePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "applies Xcode IDE base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.pluginManager.hasPlugin('dev.nokee.xcode-ide-base')
	}
}
