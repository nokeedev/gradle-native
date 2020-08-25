package dev.nokee.language.c.internal.plugins

import dev.nokee.language.base.LanguageSourceSetFactoryRegistry
import dev.nokee.language.base.LanguageSourceSetInstantiator
import dev.nokee.language.c.CHeaderSet
import dev.nokee.language.c.CSourceSet
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(CLanguageBasePlugin.class)
class CLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers language factories"() {
		when:
		project.apply plugin: CLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).creatableTypes == [CHeaderSet, CSourceSet] as Set
	}

	def "applies toolchain plugin"() {
		when:
		project.apply plugin: CLanguageBasePlugin

		then:
		project.plugins.hasPlugin(StandardToolChainsPlugin)
	}

	def "can instantiate source set"() {
		given:
		project.apply plugin: CLanguageBasePlugin
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
