package dev.nokee.model.internal

import org.gradle.api.Action
import org.gradle.api.specs.Spec
import org.junit.Assume

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
	def "can configure direct element by name"() {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		and:
		def action = Mock(Action)

		when:
		subject."${identifier1.name}" { action.execute(it) }
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * action.execute(entity1.get())
	}

	def "throws exception when configuring descendent element by name"() {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def indirectOwner = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(ownerIdentifier)
		}
		def (identifier, entity) = entity(entityDiscovered(entityIdentifier(indirectOwner)))

		and:
		def action = Mock(Action)

		when:
		subject."${identifier.name}" { action.execute(it) }
		then:
		thrown(MissingMethodException)
		and:
		0 * action.execute(_)
	}

	def "throws exception when configuring unknown element by name"() {
		given:
		def subject = newSubject()
		Assume.assumeTrue(subject instanceof HasConfigureElementByNameSupport)

		and:
		def action = Mock(Action)

		when:
		subject.foo { action.execute(it) }

		then:
		thrown(MissingMethodException)

		and:
		0 * action.execute(_)
	}
	//endregion
}
