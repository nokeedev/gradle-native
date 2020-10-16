package dev.nokee.language.base.internal.plugins

import dev.nokee.language.base.internal.KnownLanguageSourceSetFactory
import dev.nokee.language.base.internal.LanguageSourceSetConfigurer
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.base.internal.LanguageSourceSetViewFactory
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers binary configurer service"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetConfigurer) != null
	}

	def "registers binary repository service"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetRepository) != null
	}

	def "registers binary view factory"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetViewFactory) != null
	}

	def "registers known binary factory"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(KnownLanguageSourceSetFactory) != null
	}
}
