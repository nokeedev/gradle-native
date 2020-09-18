package dev.nokee.platform.base.internal.components

import dev.nokee.model.DomainObjectFactory
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.DomainObjectProvider
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

abstract class AbstractComponentContainerImplSpec extends Specification {
	@Shared def project = ProjectBuilder.builder().build()

	protected ComponentContainerImpl newSubject(ProjectIdentifier projectIdentifier = ProjectIdentifier.of(project)) {
		new ComponentContainerImpl(projectIdentifier, project.objects)
	}
}

@Subject(ComponentContainerImpl)
class ComponentContainerImplTest extends AbstractComponentContainerImplSpec {
	def "can create component container"() {
		when:
		newSubject()

		then:
		noExceptionThrown()
	}

	def "disallowing changes returns the container"() {
		given:
		def subject = newSubject()

		when:
		def result = subject.disallowChanges()

		then:
		result == subject
	}

	def "cannot resolve the all element provider before disallowing changes"() {
		given:
		def subject = newSubject()
		def provider = subject.getElements()

		when:
		provider.get()
		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'

		when:
		subject.disallowChanges()
		provider.get()
		then:
		noExceptionThrown()
	}
}

@Subject(ComponentContainerImpl)
abstract class ComponentContainerImpl_AbstractRegisterTest extends AbstractComponentContainerImplSpec {
	protected abstract <T extends Component> DomainObjectProvider<T> register(ComponentContainerImpl subject, String name, Class<T> type)

	def "throws exception when registering unknown component type"() {
		given:
		def subject = newSubject()

		when:
		register(subject, "main", UnknownComponent)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == 'Cannot create a UnknownComponent because this type is not known to component. Known types are: (None)'
	}

	def "can register known component type"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(MyComponent, Stub(DomainObjectFactory))

		when:
		register(subject, 'main', MyComponent)

		then:
		noExceptionThrown()
	}

	def "creates component only when returned provider is queried"() {
		given:
		def subject = newSubject()

		and:
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(MyComponent, factory)

		when:
		def provider = register(subject, "main", MyComponent)
		then:
		0 * factory.create(_)

		when:
		provider.get()
		then:
		1 * factory.create(_)
	}

	def "returns valid provider"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(MyComponent, { project.objects.newInstance(MyComponent) })

		when:
		def provider = register(subject, "main", MyComponent)

		then:
		provider != null
		provider.type == MyComponent
		provider.identifier instanceof ComponentIdentifier
		provider.identifier.name.get() == 'main'
		provider.identifier.type == MyComponent
		provider.identifier.projectIdentifier == ProjectIdentifier.of(project)
	}

	def "throws exception when registering additional component after disallowing changes"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(MyComponent, { project.objects.newInstance(MyComponent) })

		when:
		subject.disallowChanges()
		then:
		noExceptionThrown()

		when:
		register(subject, "main", MyComponent)
		then:
		def ex = thrown(RuntimeException)
		ex.message == 'The value cannot be changed any further.'
	}

	def "calls factory with the correct component identifier"() {
		given:
		def subject = newSubject()

		and:
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(MyComponent, factory)

		when:
		register(subject, "main", MyComponent).get()

		then:
		1 * factory.create(ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project)))
	}

	def "can get a provider of all registered components"() {
		given:
		def subject = newSubject()

		and:
		def createdComponents = []
		subject.registerFactory(MyComponent, {
			def result = project.objects.newInstance(MyComponent)
			createdComponents << result
			return result
		})

		and:
		def provider = subject.getElements()

		when:
		register(subject, "main", MyComponent)
		register(subject, "common", MyComponent)
		subject.disallowChanges()

		then:
		provider != null
		provider.get() == createdComponents as Set
	}

	interface UnknownComponent extends Component {}
	interface MyComponent extends Component {}
}

@Subject(ComponentContainerImpl)
class ComponentContainerImpl_RegisterTest extends ComponentContainerImpl_AbstractRegisterTest {

	@Override
	protected <T extends Component> DomainObjectProvider<T> register(ComponentContainerImpl subject, String name, Class<T> type) {
		return subject.register(name, type)
	}
}

@Subject(ComponentContainerImpl)
class ComponentContainerImpl_RegisterWithActionTest extends ComponentContainerImpl_AbstractRegisterTest {

	@Override
	protected <T extends Component> DomainObjectProvider<T> register(ComponentContainerImpl subject, String name, Class<T> type) {
		return subject.register(name, type, Stub(Action))
	}

	def "calls configuration action only when returned provider is queried"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(MyComponent, { project.objects.newInstance(MyComponent) })

		and:
		def action = Mock(Action)

		when:
		def provider = subject.register("main", MyComponent, action)
		then:
		0 * action.execute(_)

		when:
		provider.get()
		then:
		1 * action.execute(_)
	}
}
