package dev.nokee.language.base.internal.plugins

import dev.nokee.language.base.internal.*
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers configurer service"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetConfigurer) != null
	}

	def "registers repository service"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetRepository) != null
	}

	def "registers view factory"() {
		when:
		project.apply plugin: LanguageBasePlugin

		then:
		project.extensions.findByType(LanguageSourceSetViewFactory) != null
	}

	def "registers known factory"() {
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

	protected File file(String relativePath) {
		def result = project.file(relativePath)
		result.parentFile.mkdirs()
		result.createNewFile()
		return result
	}

	def "applies source set convention rule to all source set"() {
		given:
		project.apply plugin: LanguageBasePlugin
		def registry = project.extensions.getByType(LanguageSourceSetRegistry)

		and:
		def file1 = file('src/main/c/foo')
		def file2 = file('src/test/cpp/foo')

		when:
		def sourceSet1 = registry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('c'), LanguageSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project))))
		def sourceSet2 = registry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('cpp'), LanguageSourceSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project))))

		then:
		sourceSet1.asFileTree.files == [file1] as Set
		sourceSet2.asFileTree.files == [file2] as Set
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
