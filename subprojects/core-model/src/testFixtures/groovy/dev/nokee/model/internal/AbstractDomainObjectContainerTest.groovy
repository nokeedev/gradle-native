package dev.nokee.model.internal

import dev.nokee.model.DomainObjectFactory
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.DomainObjectProvider
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import spock.lang.Unroll

abstract class AbstractDomainObjectContainerTest<TYPE, T extends TYPE> extends AbstractDomainObjectCollectionTest<T> {
	protected abstract AbstractDomainObjectContainer<TYPE, T> newSubject()

	protected abstract TypeAwareDomainObjectIdentifier entityIdentifier(String name, Class entityType, DomainObjectIdentifier ownerIdentifier)

	def "can create component container"() {
		when:
		newSubject()
		System.nanoTime()

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

	//region register(String, Class, [Action])
	interface RegisterFunction {
		public <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type)
	}

	interface RegisterWithActionFunction {
		public <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type, Action<? super S> action)
	}

	enum RegisterNameTypeFunction implements RegisterFunction {
		INSTANCE;

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type) {
			return subject.register(name, type)
		}
	}

	enum RegisterNameTypeActionFunction implements RegisterFunction, RegisterWithActionFunction {
		INSTANCE;

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type) {
			return subject.register(name, type, new Action() {
				@Override
				void execute(Object o) {

				}
			})
		}

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type, Action<? super S> action) {
			return subject.register(name, type, action)
		}
	}

	enum RegisterNameTypeClosureFunction implements RegisterFunction, RegisterWithActionFunction {
		INSTANCE;

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type) {
			return subject.register(name, type, {})
		}

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type, Action<? super S> action) {
			return subject.register(name, type, { action.execute(it) })
		}
	}

	enum RegisterGroovyDslMethodCall implements RegisterFunction {
		INSTANCE;

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type) {
			return subject."${name}"(type)
		}
	}

	enum RegisterGroovyDslMethodWithClosureCall implements RegisterFunction, RegisterWithActionFunction {
		INSTANCE;

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type) {
			return subject."${name}"(type) {}
		}

		@Override
		def <S> DomainObjectProvider<S> call(AbstractDomainObjectContainer subject, String name, Class<S> type, Action<? super S> action) {
			return subject."${name}"(type) { action.execute(it) }
		}
	}

	protected static final List<RegisterFunction> REGISTER_FUNCTIONS_UNDER_TEST = [RegisterNameTypeFunction.INSTANCE, RegisterNameTypeActionFunction.INSTANCE, RegisterNameTypeClosureFunction.INSTANCE, RegisterGroovyDslMethodCall.INSTANCE, RegisterGroovyDslMethodWithClosureCall.INSTANCE]

	private static final List<RegisterWithActionFunction> REGISTER_WITH_ACTION_FUNCTIONS_UNDER_TEST = [RegisterNameTypeActionFunction.INSTANCE, RegisterNameTypeClosureFunction.INSTANCE, RegisterGroovyDslMethodWithClosureCall.INSTANCE]

	protected abstract Class<? extends T> getUnknownEntityType()

	@Unroll
	def "throws exception when registering unknown component type"() {
		given:
		def subject = newSubject()

		when:
		register(subject, "main", unknownEntityType)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == "Cannot create a ${unknownEntityType.simpleName} because this type is not known to test instantiator. Known types are: (None)"

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "can register known component type"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(myEntityType, Stub(DomainObjectFactory))

		when:
		register(subject, 'main', myEntityType)

		then:
		noExceptionThrown()

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	def "throws exception when binding entity type to non-creatable type"() {
		given:
		def subject = newSubject()

		when:
		subject.registerBinding(myEntityType, myEntityChildType)

		then:
		def ex = thrown(RuntimeException)
		ex.message == "Cannot bind type ${myEntityType.simpleName} because a factory for type ${myEntityChildType.simpleName} is not known to test instantiator. Known types are: (None)"
	}

	def "throws exception when binding creatable entity type"() {
		given:
		def subject = newSubject()

		when:
		subject.registerFactory(myEntityChildType, Stub(DomainObjectFactory))
		subject.registerFactory(myEntityType, Stub(DomainObjectFactory))
		subject.registerBinding(myEntityType, myEntityChildType)

		then:
		def ex = thrown(RuntimeException)
		ex.message == "Cannot bind type ${myEntityType.simpleName} because a factory for this type is already registered."
	}

	def "throws exception when registering type already binded"() {
		given:
		def subject = newSubject()

		when:
		subject.registerFactory(myEntityChildType, Stub(DomainObjectFactory))
		subject.registerBinding(myEntityType, myEntityChildType)
		subject.registerFactory(myEntityType, Stub(DomainObjectFactory))

		then:
		def ex = thrown(RuntimeException)
		ex.message == "Cannot register a factory for type ${myEntityType.simpleName} because a factory for this type is already registered."
	}

	@Unroll
	def "can create binded component type"() {
		given:
		def subject = newSubject()
		def factory = Stub(DomainObjectFactory) {
			create(_) >> { args -> entity(args[0])[1].get() }
		}

		subject.registerFactory(myEntityChildType, factory)
		subject.registerBinding(myEntityType, myEntityChildType)

		when:
		def result = register(subject, 'main', myEntityType)

		then:
		result.identifier.type == myEntityChildType
		myEntityChildType.isInstance(result.get())

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "creates component only when returned provider is queried"() {
		given:
		def subject = newSubject()

		and:
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(myEntityType, factory)

		when:
		def provider = register(subject, "main", myEntityType)
		then:
		0 * factory.create(_)

		when:
		provider.get()
		then:
		1 * factory.create(_) >> { args -> entity(args[0])[1].get() }

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "returns valid provider"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(myEntityType, { project.objects.newInstance(myEntityType) })

		when:
		def provider = register(subject, "main", myEntityType)

		then:
		provider != null
		provider.type == myEntityType
		provider.identifier == entityIdentifier('main', myEntityType, ownerIdentifier)

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when registering additional component after disallowing changes"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(myEntityType, { project.objects.newInstance(myEntityType) })

		when:
		subject.disallowChanges()
		then:
		noExceptionThrown()

		when:
		register(subject, "main", myEntityType)
		then:
		def ex = thrown(RuntimeException)
		ex.message == 'The value cannot be changed any further.'

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "calls factory with the correct component identifier"() {
		given:
		def subject = newSubject()

		and:
		def factory = Mock(DomainObjectFactory)
		subject.registerFactory(myEntityType, factory)

		when:
		register(subject, "main", myEntityType).get()

		then:
		1 * factory.create(entityIdentifier('main', myEntityType, ownerIdentifier)) >> { args -> entity(args[0])[1].get() }

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "can get a provider of all registered components"() {
		given:
		def subject = newSubject()

		and:
		def createdComponents = []
		subject.registerFactory(myEntityType, {
			def result = entity(it)[1].get()
			createdComponents << result
			return result
		})

		and:
		def provider = subject.getElements()

		when:
		register(subject, "main", myEntityType)
		register(subject, "common", myEntityType)
		subject.disallowChanges()

		then:
		provider != null
		provider.get() == createdComponents as Set

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "calls configuration action only when returned provider is queried"() {
		given:
		def subject = newSubject()

		and:
		subject.registerFactory(myEntityType, { entity(it)[1].get() })

		and:
		def action = Mock(Action)

		when:
		def provider = register(subject, "main", myEntityType, action)
		then:
		0 * action.execute(_)

		when:
		provider.get()
		then:
		1 * action.execute(_)

		where:
		register << REGISTER_WITH_ACTION_FUNCTIONS_UNDER_TEST
	}
	//endregion
}
