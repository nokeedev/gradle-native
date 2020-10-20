package dev.nokee.language.objectivec.internal.plugins

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInstantiator
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRegistry
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectiveCLanguageBasePlugin)
class ObjectiveCLanguageBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers Objective-C source set factory"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(ObjectiveCSourceSetImpl)
	}

	def "registers C header set factory"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetInstantiator).assertCreatableType(CHeaderSetImpl)
	}

	def "can applies plugin after C language base plugin"() {
		when:
		project.apply plugin: CLanguageBasePlugin
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		noExceptionThrown()
	}

	def "applies language base plugin"() {
		when:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		then:
		project.plugins.hasPlugin(LanguageBasePlugin)
	}

	def "configures backward compatible convention for fully-spelled out source set name"() {
		given:
		project.apply plugin: ObjectiveCLanguageBasePlugin

		expect:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveC'), ObjectiveCSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/main/objectiveC'), project.file('src/main/objc')] as Set
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveC'), ObjectiveCSourceSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/test/objectiveC'), project.file('src/test/objc')] as Set

		and:
		project.extensions.getByType(LanguageSourceSetRegistry).create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('obj-c'), ObjectiveCSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project)))).sourceDirectories.files == [project.file('src/main/obj-c')] as Set
	}
}
