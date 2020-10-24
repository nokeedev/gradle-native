package dev.nokee.model.internal

import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.specs.Spec
import org.junit.Assume
import spock.lang.Unroll

abstract class AbstractDomainObjectCollectionTest<T> extends DomainObjectSpec<T> {
	protected abstract Object newSubject()

	//region configureEach(Action)
	def "can configure each entities previously created"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		subject.configureEach(action)

		then:
		1 * action.execute(entity1)
		and:
		1 * action.execute(entity2)
		and:
		1 * action.execute(entity3)
		0 * action._
	}

	def "can configure each entities that will be created"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		when:
		subject.configureEach(action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * action.execute({ it == entity1.get() })

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * action.execute({ it == entity2.get() })

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * action.execute({ it == entity3.get() })
	}

	def "discovered entities are not configured"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		entityDiscovered(entityIdentifier(ownerIdentifier))
		entityDiscovered(entityIdentifier(ownerIdentifier))

		when:
		subject.configureEach(action)
		then:
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier(ownerIdentifier))
		then:
		0 * action.execute(_)
	}

	def "does not configure entities of different owner"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier())))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier())))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		0 * action.execute(_)
	}
	//endregion

	//region configureEach(Class, Action)
	def "can configure each entities previously created by type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(entityImplementationType,ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))))

		when:
		subject.configureEach(myEntityType, action)

		then:
		1 * action.execute(entity2)
		and:
		1 * action.execute(entity3)
		0 * action._
	}

	def "can configure each entities that will be created by type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))

		when:
		subject.configureEach(myEntityType, action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * action.execute({ it == entity2.get() })

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * action.execute({ it == entity3.get() })
	}

	def "discovered entities are not configured by type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))

		when:
		subject.configureEach(myEntityType, action)
		then:
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		then:
		0 * action.execute(_)
	}

	def "does not configure entities of different owner of specific type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier())))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier())))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		0 * action.execute(_)
	}
	//endregion

	//region configureEach(Spec, Action)
	def "can configure each entities previously created by spec"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		subject.configureEach(spec, action)

		then:
		1 * spec.isSatisfiedBy(entity1) >> false
		1 * spec.isSatisfiedBy(entity2) >> true
		1 * spec.isSatisfiedBy(entity3) >> false
		1 * action.execute(entity2)
		0 * action._
	}

	def "can configure each entities that will be created by spec"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		when:
		subject.configureEach(spec, action)
		then:
		0 * spec.isSatisfiedBy(_)
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * spec.isSatisfiedBy({ it == entity1.get() }) >> false
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * spec.isSatisfiedBy({ it == entity2.get() }) >> true
		1 * action.execute({ it == entity2.get() })

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * spec.isSatisfiedBy({ it == entity3.get() }) >> false
		0 * action.execute(_)
	}

	def "discovered entities are not configured by spec"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		entityDiscovered(entityIdentifier(ownerIdentifier))
		entityDiscovered(entityIdentifier(ownerIdentifier))

		when:
		subject.configureEach(spec, action)
		then:
		0 * spec.isSatisfiedBy(_)
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier(ownerIdentifier))
		then:
		0 * spec.isSatisfiedBy(_)
		0 * action.execute(_)
	}

	def "does not configure entities of different owner for matching spec"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier())))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier())))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(spec, action)
		then:
		0 * spec.isSatisfiedBy(_)
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		0 * spec.isSatisfiedBy(_)
		0 * action.execute(_)
	}
	//endregion

	//region configure element by name
	interface ConfigureFunction {
		def <T> void call(def subject, String name, Action<T> action)
	}

	interface ConfigureWithTypeFunction {
		def <S> void call(def subject, String name, Class<S> type, Action<S> action)
	}

	enum ConfigureByNameActionFunction implements ConfigureFunction {
		INSTANCE;

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject.configure(name, action)
		}
	}

	enum ConfigureByNameClosureFunction implements ConfigureFunction {
		INSTANCE;

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject.configure(name, { action.execute(it) })
		}
	}

	class ConfigureByNameTypeActionFunction implements ConfigureFunction, ConfigureWithTypeFunction {
		private final Class<?> entityType

		ConfigureByNameTypeActionFunction(Class<?> entityType) {
			this.entityType = entityType
		}

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject.configure(name, entityType, action)
		}

		@Override
		def <S> void call(def subject, String name, Class<S> type, Action<S> action) {
			subject.configure(name, type, action)
		}
	}

	class ConfigureByNameTypeClosureFunction implements ConfigureFunction, ConfigureWithTypeFunction {
		private final Class<?> entityType

		ConfigureByNameTypeClosureFunction(Class<?> entityType) {
			this.entityType = entityType
		}

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject.configure(name, entityType, { action.execute(it) })
		}

		@Override
		def <S> void call(def subject, String name, Class<S> type, Action<S> action) {
			subject.configure(name, type, { action.execute(it) })
		}
	}

	enum ConfigureGroovyDslMethodCall implements ConfigureFunction {
		INSTANCE;

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject."${name}" { action.execute(it) }
		}
	}

	class ConfigureGroovyDslMethodWithTypeCall implements ConfigureFunction, ConfigureWithTypeFunction {
		private final Class<?> entityType

		ConfigureGroovyDslMethodWithTypeCall(Class<?> entityType) {
			this.entityType = entityType
		}

		@Override
		def <T> void call(def subject, String name, Action<T> action) {
			subject."${name}"(entityType) { action.execute(it) }
		}

		@Override
		def <S> void call(def subject, String name, Class<S> type, Action<S> action) {
			subject."${name}"(type) { action.execute(it) }
		}
	}

	private static List<ConfigureFunction> CONFIGURE_FUNCTIONS_UNDER_TEST
	private static List<ConfigureFunction> CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST

	def setupSpec() {
		CONFIGURE_FUNCTIONS_UNDER_TEST = [ConfigureByNameActionFunction.INSTANCE, new ConfigureByNameTypeActionFunction(entityType), ConfigureByNameClosureFunction.INSTANCE, new ConfigureByNameTypeClosureFunction(entityType), ConfigureGroovyDslMethodCall.INSTANCE, new ConfigureGroovyDslMethodWithTypeCall(entityType)]
		CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST = [new ConfigureByNameTypeActionFunction(entityType), new ConfigureByNameTypeClosureFunction(entityType), new ConfigureGroovyDslMethodWithTypeCall(entityType)]
	}

	@Unroll
	def "can configure direct element by name"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		and:
		def action = Mock(Action)

		when:
		configure(subject, identifier1.name.get(), action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * action.execute(entity1.get())

		where:
		configure << CONFIGURE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "can configure direct element by name and type"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		and:
		def action = Mock(Action)

		when:
		configure(subject, identifier1.name.get(), identifier1.type, action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * action.execute(entity1.get())

		where:
		configure << CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when configuring element by name and with wrong type"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		and:
		def action = Mock(Action)

		when:
		configure(subject, identifier1.name.get(), String, action)
		then:
		def ex = thrown(RuntimeException)
		[
			(InvalidUserDataException.class): { ["The domain object '${identifier1.name}' (${entityImplementationType.canonicalName}) directly owned by ${ownerIdentifier} is not a subclass of the given type (java.lang.String).".toString(), "Cannot create a String because this type is not known to test instantiator. Known types are: (None)".toString()].contains(it) },
			(MissingMethodException.class): { it.startsWith("No signature of method: ${subject.class.canonicalName}.${identifier1.name}() is applicable for argument types") }
		].get(ex.class)(ex.message)
		0 * action.execute(_)
				//{ it == "Cannot create a String because this type is not known to test instantiator. Known types are: (None)" },

		where:
		configure << CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when configuring descendent element by name"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)
		Assume.assumeTrue('components cannot be nested', !entityIdentifier(ownerIdentifier()).class.simpleName.equals('ComponentIdentifier'))

		and:
		def indirectOwner = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(ownerIdentifier)
		}
		def (identifier, entity) = entity(entityDiscovered(entityIdentifier(indirectOwner)))

		and:
		def action = Mock(Action)

		when:
		configure(subject, identifier.name.get(), action)
		then:
		def ex = thrown(RuntimeException)
		[
			(UnknownDomainObjectException.class): { it == "${subject.elementType.simpleName} with name '${identifier.name}' and directly owned by ${ownerIdentifier} not found." },
			(MissingMethodException.class): { it.startsWith("No signature of method: ${subject.class.canonicalName}.${identifier.name}() is applicable for argument types")}
		].get(ex.class)(ex.message)
		and:
		0 * action.execute(_)

		where:
		configure << CONFIGURE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when configuring descendent element by name and type"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)
		Assume.assumeTrue('components cannot be nested', !entityIdentifier(ownerIdentifier()).class.simpleName.equals('ComponentIdentifier'))

		and:
		def indirectOwner = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(ownerIdentifier)
		}
		def (identifier, entity) = entity(entityDiscovered(entityIdentifier(indirectOwner)))

		and:
		def action = Mock(Action)

		when:
		configure(subject, identifier.name.get(), identifier.type, action)
		then:
		def ex = thrown(RuntimeException)
		[
			(UnknownDomainObjectException.class): { it == "${subject.elementType.simpleName} with name '${identifier.name}' and directly owned by ${ownerIdentifier} not found." },
			(MissingMethodException.class): { it.startsWith("No signature of method: ${subject.class.canonicalName}.${identifier.name}() is applicable for argument types") }
		].get(ex.class)(ex.message)
		and:
		0 * action.execute(_)

		where:
		configure << CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when configuring unknown element by name"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def action = Mock(Action)

		when:
		configure(subject, 'foo', action)

		then:
		def ex = thrown(RuntimeException)
		[
			(UnknownDomainObjectException.class): { it == "${subject.elementType.simpleName} with name 'foo' and directly owned by ${ownerIdentifier} not found." },
			(InvalidUserDataException.class): { it == "Cannot create a ${entityType.simpleName} because this type is not known to test instantiator. Known types are: (None)" },
			(MissingMethodException.class): { it.startsWith("No signature of method: ${subject.class.canonicalName}.foo() is applicable for argument types") }
		].get(ex.class)(ex.message)

		and:
		0 * action.execute(_)

		where:
		configure << CONFIGURE_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "throws exception when configuring unknown element by name and type"(configure) {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def action = Mock(Action)

		when:
		configure(subject, 'foo', entityType, action)

		then:
		def ex = thrown(RuntimeException)
		[
			(UnknownDomainObjectException.class): { it == "${subject.elementType.simpleName} with name 'foo' and directly owned by ${ownerIdentifier} not found." },
			(InvalidUserDataException.class): { it == "Cannot create a ${entityType.simpleName} because this type is not known to test instantiator. Known types are: (None)" },
			(MissingMethodException.class): { it.startsWith("No signature of method: ${subject.class.canonicalName}.foo() is applicable for argument types") }
		].get(ex.class)(ex.message)

		and:
		0 * action.execute(_)

		where:
		configure << CONFIGURE_WITH_TYPE_FUNCTIONS_UNDER_TEST
	}
	//endregion
}
