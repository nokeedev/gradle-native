package dev.nokee.language.base.internal.plugins

import dev.nokee.language.base.internal.*
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

	def "registers generic source set factory"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(LanguageSourceSetImpl)
	}

	def "registers instantiator"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetInstantiator) != null
	}

	def "registers registry"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetRegistry) != null
	}
}
