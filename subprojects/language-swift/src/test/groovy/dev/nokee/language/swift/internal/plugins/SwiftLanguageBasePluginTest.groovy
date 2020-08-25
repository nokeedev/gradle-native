package dev.nokee.language.swift.internal.plugins

import dev.nokee.language.base.LanguageSourceSetInstantiator
import dev.nokee.language.swift.SwiftSourceSet
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(SwiftLanguageBasePlugin)
class SwiftLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers language factories"() {
		when:
		project.apply plugin: SwiftLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).creatableTypes == [SwiftSourceSet] as Set
	}

	def "applies toolchain plugin"() {
		when:
		project.apply plugin: SwiftLanguagePlugin

		then:
		project.plugins.hasPlugin(SwiftCompilerPlugin)
	}

	def "can instantiate source set"() {
		given:
		project.apply plugin: SwiftLanguageBasePlugin
		def instantiator = project.extensions.getByType(LanguageSourceSetInstantiator)
		def identifier = Mock(DomainObjectIdentifier)

		expect:
		instantiator.creatableTypes.each { type ->
			def sourceSet = instantiator.create(identifier, type)
			assert sourceSet != null
			assert type.isAssignableFrom(sourceSet.class)
		}
	}
}
