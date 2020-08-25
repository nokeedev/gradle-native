package dev.nokee.language.objectivecpp.internal.plugins

import dev.nokee.language.base.LanguageSourceSetInstantiator
import dev.nokee.language.cpp.CppHeaderSet
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCppLanguageBasePlugin)
class ObjectiveCppLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers language factories"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).creatableTypes == [CppHeaderSet, ObjectiveCppSourceSet] as Set
	}

	def "applies toolchain plugin"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		project.plugins.hasPlugin(StandardToolChainsPlugin)
	}

	def "can instantiate source set"() {
		given:
		project.apply plugin: ObjectiveCppLanguageBasePlugin
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
