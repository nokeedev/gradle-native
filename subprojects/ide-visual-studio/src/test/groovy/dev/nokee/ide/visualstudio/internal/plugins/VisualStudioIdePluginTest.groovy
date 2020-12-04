package dev.nokee.ide.visualstudio.internal.plugins


import spock.lang.Specification
import spock.lang.Subject

@Subject(VisualStudioIdePlugin)
class VisualStudioIdePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies Visual Studio IDE base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide'

		then:
		project.pluginManager.hasPlugin('dev.nokee.visual-studio-ide-base')
	}
}
