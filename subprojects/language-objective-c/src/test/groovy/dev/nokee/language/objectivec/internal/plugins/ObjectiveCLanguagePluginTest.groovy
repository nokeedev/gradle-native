package dev.nokee.language.objectivec.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInternal
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl
import dev.nokee.model.internal.DomainObjectDiscovered
import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy

@Subject(ObjectiveCLanguagePlugin)
class ObjectiveCLanguagePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies Objective-C language base plugin"() {
		when:
		project.apply plugin: ObjectiveCLanguagePlugin

		then:
		project.plugins.hasPlugin(ObjectiveCLanguageBasePlugin)
	}

	def "creates Objective-C source set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: ObjectiveCLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('objectiveC'), ObjectiveCSourceSetImpl, identifier) }
	}

	def "creates C header set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: ObjectiveCLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers'), CHeaderSetImpl, identifier) }
	}

	interface MyComponent extends Component, HasNativeLanguageSupport {}
}
