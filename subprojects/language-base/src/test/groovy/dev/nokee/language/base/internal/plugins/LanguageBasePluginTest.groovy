package dev.nokee.language.base.internal.plugins

import dev.nokee.language.base.internal.LanguageSourceSetFactoryRegistry
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(LanguageBasePlugin)
class LanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers singleton instance on project"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetFactoryRegistry)
		project.extensions.findByType(LanguageSourceSetInstantiator)
	}
}
