package dev.nokee.language.objectivec.internal.plugins

import dev.nokee.language.base.LanguageSourceSetInstantiator
import dev.nokee.language.c.CHeaderSet
import dev.nokee.language.objectivec.ObjectiveCSourceSet
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCLanguageBasePlugin)
class ObjectiveCLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers language factories"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).creatableTypes == [CHeaderSet, ObjectiveCSourceSet] as Set
	}

	def "applies toolchain plugin"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		project.plugins.hasPlugin(StandardToolChainsPlugin)
	}

	def "can instantiate source set"() {
		given:
		project.apply plugin: ObjectiveCLanguageBasePlugin
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
