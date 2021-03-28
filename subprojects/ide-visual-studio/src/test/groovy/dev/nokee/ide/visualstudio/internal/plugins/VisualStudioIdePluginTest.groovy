package dev.nokee.ide.visualstudio.internal.plugins

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(VisualStudioIdePlugin)
class VisualStudioIdePluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	def "applies Visual Studio IDE base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide'

		then:
		project.pluginManager.hasPlugin('dev.nokee.visual-studio-ide-base')
	}
}
