package dev.nokee.language.cpp.internal.plugins

import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.cpp.CppHeaderSet
import dev.nokee.language.cpp.CppSourceSet
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(CppLanguageBasePlugin.class)
class CppLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers language factories"() {
		when:
		project.apply plugin: CppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).creatableTypes == [CppHeaderSet, CppSourceSet] as Set
	}

	def "applies toolchain plugin"() {
		when:
		project.apply plugin: CppLanguageBasePlugin

		then:
		project.plugins.hasPlugin(StandardToolChainsPlugin)
	}

	def "can instantiate source set"() {
		given:
		project.apply plugin: CppLanguageBasePlugin
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
