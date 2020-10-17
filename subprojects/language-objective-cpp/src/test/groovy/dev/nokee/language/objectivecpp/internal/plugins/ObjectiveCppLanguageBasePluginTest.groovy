package dev.nokee.language.objectivecpp.internal.plugins

import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCppLanguageBasePlugin)
class ObjectiveCppLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers Objective-C++ source set factory"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(ObjectiveCppSourceSetImpl)
	}

	def "registers C++ header set factory"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CppHeaderSetImpl)
	}

	def "can applies plugin after C++ language base plugin"() {
		when:
		project.apply plugin: CppLanguageBasePlugin
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		noExceptionThrown()
	}

	def "applies language base plugin"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}
}
