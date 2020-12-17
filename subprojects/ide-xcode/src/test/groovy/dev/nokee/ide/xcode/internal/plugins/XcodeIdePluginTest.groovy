package dev.nokee.ide.xcode.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(XcodeIdePlugin)
class XcodeIdePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies Xcode IDE base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.pluginManager.hasPlugin('dev.nokee.xcode-ide-base')
	}
}
