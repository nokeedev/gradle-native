package dev.nokee.language.objectivecpp.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInternal
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import dev.nokee.model.internal.DomainObjectDiscovered
import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy

@Subject(ObjectiveCppLanguagePlugin)
class ObjectiveCppLanguagePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies Objective-C++ language base plugin"() {
		when:
		project.apply plugin: ObjectiveCppLanguagePlugin

		then:
		project.plugins.hasPlugin(ObjectiveCppLanguageBasePlugin)
	}

	def "creates Objective-C++ source set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: ObjectiveCppLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveCpp'), ObjectiveCppSourceSetImpl, identifier) }
	}

	def "creates C header set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: ObjectiveCppLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers'), CppHeaderSetImpl, identifier) }
	}

	interface MyComponent extends Component, HasNativeLanguageSupport {}
}
