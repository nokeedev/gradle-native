package dev.nokee.language.base.internal

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.DomainObjectCreated
import dev.nokee.model.internal.DomainObjectDiscovered
import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import org.gradle.api.Action
import spock.lang.Specification

class LanguageSourceSetRegistryTest extends Specification implements LanguageSourceSetFixture {
	protected DomainObjectIdentifier newComponentOwner() {
		return ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('test'))
	}

	def "can create source set owned by a component"() {
		given:
		def eventPublisher = Mock(DomainObjectEventPublisher)
		def instantiator = Mock(LanguageSourceSetInstantiator)
		def subject = new LanguageSourceSetRegistry(eventPublisher, instantiator)
		def identifier = entityIdentifier(MyLanguageSourceSet, newComponentOwner())
		def sourceSet = Stub(MyLanguageSourceSet)

		when:
		def result = subject.create(identifier)

		then:
		1 * eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
		and:
		1 * instantiator.newInstance(identifier, MyLanguageSourceSet) >> sourceSet
		and:
		1 * eventPublisher.publish(new DomainObjectCreated<>(identifier, sourceSet))
		and:
		result == sourceSet
	}

	def "can create source set owned by a component with action"() {
		given:
		def eventPublisher = Mock(DomainObjectEventPublisher)
		def instantiator = Mock(LanguageSourceSetInstantiator)
		def subject = new LanguageSourceSetRegistry(eventPublisher, instantiator)
		def identifier = entityIdentifier(MyLanguageSourceSet, newComponentOwner())
		def sourceSet = Stub(MyLanguageSourceSet)

		and:
		def action = Mock(Action)

		when:
		def result = subject.create(identifier, action)

		then:
		1 * eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
		and:
		1 * instantiator.newInstance(identifier, MyLanguageSourceSet) >> sourceSet
		and:
		1 * eventPublisher.publish(new DomainObjectCreated<>(identifier, sourceSet))
		and:
		1 * action.execute(sourceSet)
		and:
		result == sourceSet
	}

	interface MyLanguageSourceSet extends LanguageSourceSet {}
}
