package dev.nokee.language.c.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.c.internal.CSourceSetImpl
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin
import spock.lang.Specification
import spock.lang.Subject

@Subject(CLanguageBasePlugin)
class CLanguageBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "registers C source set factory"() {
		when:
		project.apply plugin: CLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CSourceSetImpl)
	}

	def "registers C header set factory"() {
		when:
		project.apply plugin: CLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CHeaderSetImpl)
	}

	def "can applies plugin after Objective-C language base plugin"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin
		project.apply plugin: CLanguageBasePlugin

		then:
		noExceptionThrown()
	}

	def "applies language base plugin"() {
		when:
		project.apply plugin: CLanguageBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}
}
