package dev.nokee.platform.base.internal.components

import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.AbstractDomainObjectContainer
import dev.nokee.model.internal.AbstractDomainObjectContainerTest
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.testing.base.TestSuiteComponent
import org.gradle.api.Action
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ComponentContainerImpl)
class ComponentContainerImplTest extends AbstractDomainObjectContainerTest<Component, Component> implements ComponentFixture {
	private ComponentInstantiator entityInstantiator = newEntityInstantiator()

	@Override
	protected AbstractDomainObjectContainer<Component, Component> newSubject() {
		return new ComponentContainerImpl(ownerIdentifier, entityConfigurer, eventPublisher, newEntityProviderFactory(), entityRepository, newEntityFactory(), entityInstantiator)
	}

	@Override
	protected TypeAwareDomainObjectIdentifier entityIdentifier(String name, Class entityType, DomainObjectIdentifier ownerIdentifier) {
		return ComponentIdentifier.of(ComponentName.of(name), entityType, ownerIdentifier)
	}

	//region
	def "throws exception when registering test suite component factory"() {
		given:
		def subject = newSubject()

		when:
		subject.registerFactory(MyTestSuiteComponent, Stub(DomainObjectFactory))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot register test suite component types in this container, use a TestSuiteContainer instead."
	}

	def "throws exception when binding test suite component to component type"() {
		given:
		def subject = newSubject()
		subject.registerFactory(entityImplementationType, Stub(DomainObjectFactory))

		when:
		subject.registerBinding(MyTestSuiteComponent, entityImplementationType)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot bind test suite component types in this container, use a TestSuiteContainer instead."
	}

	def "throws exception when binding component to already registered test suite component type"() {
		given:
		def subject = newSubject()
		entityInstantiator.registerFactory(MyTestSuiteComponent, Stub(DomainObjectFactory))

		when:
		subject.registerBinding(entityImplementationType, MyTestSuiteComponent)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot bind to test suite component types in this container, use a TestSuiteContainer instead."
	}

	@Unroll
	def "throws exception when registering test suite components"(register) {
		given:
		def subject = newSubject()
		entityInstantiator.registerFactory(MyTestSuiteComponent, Stub(DomainObjectFactory))

		when:
		register(subject, 'main', MyTestSuiteComponent)
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Cannot register test suite components in this container, use a TestSuiteContainer instead."

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	def "cannot configure test suite component"() {
		given:
		def subject = newSubject()
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(MyTestSuiteComponent, ownerIdentifier))))
		def action = Mock(Action)

		when:
		subject.configure(identifier.name.get(), action)
		subject.configure(identifier.name.get()) { action.execute(it) }
		subject."${identifier.name}" { action.execute(it) }
		subject.configure(identifier.name.get(), entityType, action)
		subject.configure(identifier.name.get(), entityType) { action.execute(it) }
		subject."${identifier.name}"(entityType) { action.execute(it) }
		subject.configureEach(action)
		subject.configureEach { action.execute(it) }
		subject.configureEach(entityType, action)
		subject.configureEach(entityType) { action.execute(it) }
		subject.configureEach({ true }) { action.execute(it) }
		subject.whenElementKnown { action.execute(it) }
		subject.whenElementKnown(entityType) { action.execute(it) }

		then:
		0 * action.execute(_)
	}
	//endregion

	@Override
	protected Class<? extends Component> getUnknownEntityType() {
		return UnknownComponent
	}

	interface UnknownComponent extends Component {}
	static class MyTestSuiteComponent implements TestSuiteComponent {
		@Override
		TestSuiteComponent testedComponent(Object component) {
			throw new UnsupportedOperationException()
		}
	}
}
