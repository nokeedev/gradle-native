package dev.nokee.language.cpp.internal.plugins

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRegistry
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.cpp.internal.CppSourceSetImpl
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(CppLanguageBasePlugin)
class CppLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers C++ source set factory"() {
		when:
		project.apply plugin: CppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CppSourceSetImpl)
	}

	def "registers C++ header set factory"() {
		when:
		project.apply plugin: CppLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CppHeaderSetImpl)
	}

	def "can applies plugin after Objective-C++ language base plugin"() {
		when:
		project.apply plugin: ObjectiveCppLanguageBasePlugin
		project.apply plugin: CppLanguageBasePlugin

		then:
		noExceptionThrown()
	}

	def "applies language base plugin"() {
		when:
		project.apply plugin: CppLanguageBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}

	def "include .h files in C++ headers sets"() {
		given:
		project.apply plugin: CppLanguageBasePlugin

		expect:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers1'), CppHeaderSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).filter.includes.contains('**/*.h')
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers2'), CppHeaderSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project)))).filter.includes.contains('**/*.h')
	}
}
