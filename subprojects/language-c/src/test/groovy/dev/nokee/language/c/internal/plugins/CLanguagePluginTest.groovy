package dev.nokee.language.c.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInternal
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.c.internal.CSourceSetImpl
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport
import dev.nokee.model.internal.DomainObjectDiscovered
import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy

@Subject(CLanguagePlugin)
class CLanguagePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies C language base plugin"() {
		when:
		project.apply plugin: CLanguagePlugin

		then:
		project.plugins.hasPlugin(CLanguageBasePlugin)
	}

	def "creates C source set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: CLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('c'), CSourceSetImpl, identifier) }
	}

	def "creates C header set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: CLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers'), CHeaderSetImpl, identifier) }
	}

	interface MyComponent extends Component, HasNativeLanguageSupport {}
}
