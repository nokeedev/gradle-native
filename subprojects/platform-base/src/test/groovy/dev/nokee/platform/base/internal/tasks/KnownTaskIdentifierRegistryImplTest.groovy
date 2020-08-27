package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.DefaultTask
import spock.lang.Specification
import spock.lang.Subject

@Subject(KnownTaskIdentifierRegistryImpl)
class KnownTaskIdentifierRegistryImplTest extends Specification {
	def "can add identifier to registry"() {
		given:
		def subject = new KnownTaskIdentifierRegistryImpl()

		when:
		subject.add(identifierOf('foo'))

		then:
		noExceptionThrown()
	}

	def "can retrieve a list of previously added task name from registry"() {
		given:
		def owner = new ProjectIdentifier('root')
		def subject = new KnownTaskIdentifierRegistryImpl()

		when:
		subject.add(identifierOf('foo', owner))
		subject.add(identifierOf('bar', owner))
		subject.add(identifierOf('far', owner))

		then:
		subject.getTaskNamesFor(owner) == ['foo', 'bar', 'far'] as Set
	}

	def "can retrieve a sub-list by owner identifier of previously added task name from registry"() {
		given:
		def ownerProject = new ProjectIdentifier('root')
		def ownerComponent = new ComponentIdentifier('test', ownerProject)
		def ownerVariant = new VariantIdentifier('macosDebug', ownerComponent)
		def subject = new KnownTaskIdentifierRegistryImpl()

		when:
		subject.add(identifierOf('foo', ownerProject))
		subject.add(identifierOf('bar', ownerComponent))
		subject.add(identifierOf('far', ownerVariant))

		then:
		subject.getTaskNamesFor(ownerProject) == ['foo', 'bar', 'far'] as Set
		subject.getTaskNamesFor(ownerComponent) == ['bar', 'far'] as Set
		subject.getTaskNamesFor(ownerVariant) == ['far'] as Set
	}

	TaskIdentifier<?> identifierOf(String taskName, DomainObjectIdentifierInternal parentIdentifier = new ProjectIdentifier()) {
		return new TaskIdentifier<DummyTask>(taskName, DummyTask, parentIdentifier)
	}

	static class DummyTask extends DefaultTask {}
}
