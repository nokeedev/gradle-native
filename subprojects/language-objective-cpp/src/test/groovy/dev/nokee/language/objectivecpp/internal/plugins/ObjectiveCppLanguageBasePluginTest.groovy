package dev.nokee.language.objectivecpp.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRegistry
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCppLanguageBasePlugin)
class ObjectiveCppLanguageBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

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

	def "configures backward compatible convention for fully-spelled out source set name"() {
		given:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		expect:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveCpp'), ObjectiveCppSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/main/objectiveCpp'), project.file('src/main/objcpp')] as Set
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveCpp'), ObjectiveCppSourceSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/test/objectiveCpp'), project.file('src/test/objcpp')] as Set

		and:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('obj-cpp'), ObjectiveCppSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/main/obj-cpp')] as Set
	}

	def "include .h files in C++ headers sets"() {
		given:
		project.apply plugin: ObjectiveCppLanguageBasePlugin

		expect:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers1'), CppHeaderSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).filter.includes.contains('**/*.h')
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers2'), CppHeaderSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project)))).filter.includes.contains('**/*.h')
	}
}
