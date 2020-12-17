package dev.nokee.language.swift.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.swift.internal.SwiftSourceSetImpl
import spock.lang.Specification
import spock.lang.Subject

@Subject(SwiftLanguageBasePlugin)
class SwiftLanguageBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "registers swift source set factory"() {
		when:
		project.apply plugin: SwiftLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(SwiftSourceSetImpl)
	}

	def "applies language base plugin"() {
		when:
		project.apply plugin: SwiftLanguageBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}
}
