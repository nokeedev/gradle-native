package dev.nokee.language.cpp.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetInternal
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.cpp.internal.CppSourceSetImpl
import dev.nokee.language.nativebase.internal.HasNativeLanguageSupport
import dev.nokee.model.internal.DomainObjectDiscovered
import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy

@Subject(CppLanguagePlugin)
class CppLanguagePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies C++ language base plugin"() {
		when:
		project.apply plugin: CppLanguagePlugin

		then:
		project.plugins.hasPlugin(CppLanguageBasePlugin)
	}

	def "creates C++ source set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: CppLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('cpp'), CppSourceSetImpl, identifier) }
	}

	def "creates C++ header set upon discovering entity implementing HasNativeLanguageSupport"() {
		given:
		project.apply plugin: CppLanguagePlugin
		def eventPublisher = project.extensions.getByType(DomainObjectEventPublisher)
		def identifier = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))

		when:
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(directlyOwnedBy(identifier)).any {((LanguageSourceSetInternal)it).identifier == LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('headers'), CppHeaderSetImpl, identifier) }
	}

	interface MyComponent extends Component, HasNativeLanguageSupport {}
}
