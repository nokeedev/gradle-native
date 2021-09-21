/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal


import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.UnknownDomainObjectException
import org.junit.Assume

abstract class AbstractDomainObjectConfigurerTest<T> extends DomainObjectSpec<T> {
	protected abstract DomainObjectConfigurer<T> newSubject()

	protected abstract Class<? extends T> getMyEntityType()

	//region configureEach
	def "does not call configure each action when no entities are created"() {
		given:
		def subject = newSubject()
		def owner = Stub(DomainObjectIdentifierInternal)
		def action = Mock(Action)

		when:
		subject.configureEach(owner, entityType, action)

		then:
		0 * action.execute(_)
	}

	def "calls configure each action for each entities previously created"() {
		given:
		def subject = newSubject()

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		and:
		def action = Mock(Action)

		when:
		subject.configureEach(ownerIdentifier, entityType, action)

		then:
		1 * action.execute(entity1)
		and:
		1 * action.execute(entity2)
		and:
		1 * action.execute(entity3)
		and:
		0 * action.execute(_)
	}

	def "calls configure each action for each entities previously created of the matching owner"() {
		given:
		def subject = newSubject()

		and:
		def owner1 = ownerIdentifier('main')
		def owner2 = ownerIdentifier('test')
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(owner1))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(owner2))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(owner1))))

		and:
		def actionOwner1 = Mock(Action)
		def actionOwner2 = Mock(Action)

		when:
		subject.configureEach(owner1, entityType, actionOwner1)
		then:
		1 * actionOwner1.execute(entity1)
		and:
		1 * actionOwner1.execute(entity3)
		and:
		0 * actionOwner1.execute(_)

		when:
		subject.configureEach(owner2, entityType, actionOwner2)
		then:
		1 * actionOwner2.execute(entity2)
		and:
		0 * actionOwner2.execute(_)
	}

	def "calls configure each action for each entities previously created of the matching type"() {
		given:
		def subject = newSubject()

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier))))

		and:
		def action = Mock(Action)

		when:
		subject.configureEach(ownerIdentifier, myEntityType, action)
		then:
		1 * action.execute(entity2)
		and:
		0 * action.execute(_)
	}

	def "calls configure each action for each created entities"() {
		given:
		def subject = newSubject()

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))

		and:
		def action = Mock(Action)
		subject.configureEach(ownerIdentifier, entityType, action)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * action.execute({ it == entity1.get() })
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * action.execute({ it == entity2.get() })
		0 * action.execute(_)

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * action.execute({ it == entity3.get() })
		0 * action.execute(_)
	}

	def "calls configure each action for each created entities of the matching owner"() {
		given:
		def subject = newSubject()

		and:
		def owner1 = ownerIdentifier()
		def owner2 = ownerIdentifier()
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(owner1)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(owner2)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(owner1)))

		and:
		def actionOwner1 = Mock(Action)
		def actionOwner2 = Mock(Action)
		subject.configureEach(owner1, entityType, actionOwner1)
		subject.configureEach(owner2, entityType, actionOwner2)

		when:
		entityCreated(identifier1, entity1)
		then:
		1 * actionOwner1.execute({ it == entity1.get() })
		0 * actionOwner1.execute(_)
		and:
		0 * actionOwner2.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		0 * actionOwner1.execute(_)
		and:
		1 * actionOwner2.execute({ it == entity2.get() })
		0 * actionOwner2.execute(_)

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * actionOwner1.execute({ it == entity3.get() })
		0 * actionOwner1.execute(_)
		and:
		0 * actionOwner2.execute(_)
	}

	def "calls configure each action for each created entities of the matching type"() {
		given:
		def subject = newSubject()

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier)))

		and:
		def action = Mock(Action)
		subject.configureEach(ownerIdentifier, myEntityType, action)

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
		0 * action.execute(_)
	}
	//endregion

	//region configure by identifier
	def "throw exception when configuring unknown identifier"() {
		given:
		def subject = newSubject()
		def identifier = entityIdentifier(ownerIdentifier)

		when:
		subject.configure(identifier, Stub(Action))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown entity identified as ${identifier}."
	}

	def "can configure known entity previously created"() {
		given:
		def subject = newSubject()

		and:
		def (entityIdentifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (entityIdentifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		and:
		def action = Mock(Action)

		when:
		subject.configure(entityIdentifier1, action)

		then:
		1 * action.execute(entity1)
		0 * action.execute(_)
	}

	def "can configure known entity discovered later created"() {
		given:
		def subject = newSubject()

		and:
		def (entityIdentifier, entity) = entity(entityIdentifier(ownerIdentifier))

		and:
		def action = Mock(Action)

		when:
		entityDiscovered(entityIdentifier)
		subject.configure(entityIdentifier, action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(entityIdentifier, entity)
		then:
		1 * action.execute({ it == entity.get() })
		0 * action.execute(_)
	}

	def "calls configuration action in order they were registered when mixing configure each and single configure"() {
		given:
		def subject = newSubject()

		and:
		def (entityIdentifier, entity) = entity(entityIdentifier(myEntityType, ownerIdentifier))

		and:
		def action1 = Mock(Action)
		def action2 = Mock(Action)
		def action3 = Mock(Action)
		def action4 = Mock(Action)

		when:
		entityDiscovered(entityIdentifier)
		subject.configure(entityIdentifier, action1)
		subject.configureEach(ownerIdentifier, entityType, action2)
		subject.configureEach(ownerIdentifier, myEntityType, action3)
		subject.configure(entityIdentifier, action4)
		entityCreated(entityIdentifier, entity)

		then:
		1 * action1.execute({ it == entity.get() })
		and:
		1 * action2.execute({ it == entity.get() })
		and:
		1 * action3.execute({ it == entity.get() })
		and:
		1 * action4.execute({ it == entity.get() })
		0 * _
	}
	//endregion configure

	//region whenElementKnown
	def "does not call known element action when no entities are discovered"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		when:
		subject.whenElementKnown(ownerIdentifier, entityType, action)

		then:
		0 * action.execute(_)
	}

	def "calls known element action for each entities previously discovered"() {
		given:
		def subject = newSubject()

		and:
		def entityIdentifier1 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def entityIdentifier2 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def entityIdentifier3 = entityDiscovered(entityIdentifier(ownerIdentifier))

		and:
		def action = Mock(Action)

		when:
		subject.whenElementKnown(ownerIdentifier, entityType, action)

		then:
		1 * action.execute(entityIdentifier1)
		and:
		1 * action.execute(entityIdentifier2)
		and:
		1 * action.execute(entityIdentifier3)
		and:
		0 * action.execute(_)
	}

	def "calls known element action for each entities previously discovered of the matching owner"() {
		given:
		def subject = newSubject()

		and:
		def owner1 = ownerIdentifier()
		def owner2 = ownerIdentifier()
		def entityIdentifier1 = entityDiscovered(entityIdentifier(owner1))
		def entityIdentifier2 = entityDiscovered(entityIdentifier(owner2))
		def entityIdentifier3 = entityDiscovered(entityIdentifier(owner1))

		and:
		def actionOwner1 = Mock(Action)
		def actionOwner2 = Mock(Action)

		when:
		subject.whenElementKnown(owner1, entityType, actionOwner1)
		then:
		1 * actionOwner1.execute(entityIdentifier1)
		and:
		1 * actionOwner1.execute(entityIdentifier3)
		and:
		0 * actionOwner1.execute(_)

		when:
		subject.whenElementKnown(owner2, entityType, actionOwner2)
		then:
		1 * actionOwner2.execute(entityIdentifier2)
		and:
		0 * actionOwner2.execute(_)
	}

	def "calls known element action for each entities previously discovered of the matching type"() {
		given:
		def subject = newSubject()

		and:
		def entityIdentifier1 = entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier))
		def entityIdentifier2 = entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		def entityIdentifier3 = entityDiscovered(entityIdentifier(entityImplementationType, ownerIdentifier))

		and:
		def action = Mock(Action)

		when:
		subject.whenElementKnown(ownerIdentifier, myEntityType, action)
		then:
		1 * action.execute(entityIdentifier2)
		and:
		0 * action.execute(_)
	}

	def "calls known element action for each created entities"() {
		given:
		def subject = newSubject()

		and:
		def entityIdentifier1 = entityIdentifier(ownerIdentifier)
		def entityIdentifier2 = entityIdentifier(ownerIdentifier)
		def entityIdentifier3 = entityIdentifier(ownerIdentifier)

		and:
		def action = Mock(Action)
		subject.whenElementKnown(ownerIdentifier, entityType, action)

		when:
		entityDiscovered(entityIdentifier1)
		then:
		1 * action.execute(entityIdentifier1)
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier2)
		then:
		1 * action.execute(entityIdentifier2)
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier3)
		then:
		1 * action.execute(entityIdentifier3)
		0 * action.execute(_)
	}

	def "calls when element known action for each created entities of the matching owner"() {
		given:
		def subject = newSubject()

		and:
		def owner1 = ownerIdentifier()
		def owner2 = ownerIdentifier()
		def entityIdentifier1 = entityIdentifier(owner1)
		def entityIdentifier2 = entityIdentifier(owner2)
		def entityIdentifier3 = entityIdentifier(owner1)

		and:
		def actionOwner1 = Mock(Action)
		def actionOwner2 = Mock(Action)
		subject.whenElementKnown(owner1, entityType, actionOwner1)
		subject.whenElementKnown(owner2, entityType, actionOwner2)

		when:
		entityDiscovered(entityIdentifier1)
		then:
		1 * actionOwner1.execute(entityIdentifier1)
		0 * actionOwner1.execute(_)
		and:
		0 * actionOwner2.execute(_)

		when:
		entityDiscovered(entityIdentifier2)
		then:
		0 * actionOwner1.execute(_)
		and:
		1 * actionOwner2.execute(entityIdentifier2)
		0 * actionOwner2.execute(_)

		when:
		entityDiscovered(entityIdentifier3)
		then:
		1 * actionOwner1.execute(entityIdentifier3)
		0 * actionOwner1.execute(_)
		and:
		0 * actionOwner2.execute(_)
	}

	def "calls when element known action for each created entities of the matching type"() {
		given:
		def subject = newSubject()

		and:
		def entityIdentifier1 = entityIdentifier(entityImplementationType, ownerIdentifier)
		def entityIdentifier2 = entityIdentifier(myEntityType, ownerIdentifier)
		def entityIdentifier3 = entityIdentifier(entityImplementationType, ownerIdentifier)

		and:
		def action = Mock(Action)
		subject.whenElementKnown(ownerIdentifier, myEntityType, action)

		when:
		entityDiscovered(entityIdentifier1)
		then:
		0 * action.execute(_)

		when:
		entityDiscovered(entityIdentifier2)
		then:
		1 * action.execute(entityIdentifier2)

		when:
		entityDiscovered(entityIdentifier3)
		then:
		0 * action.execute(_)
	}

	def "can discover entity inside element known callback"() {
		given:
		def subject = newSubject()

		and:
		def entityIdentifier1 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def entityIdentifier2 = entityIdentifier(ownerIdentifier)

		and:
		def action = Mock(Action)

		when:
		subject.whenElementKnown(ownerIdentifier, entityType, action)

		then:
		1 * action.execute(entityIdentifier1) >> { entityDiscovered(entityIdentifier2) }
		and:
		1 * action.execute(entityIdentifier2)
		and:
		0 * action.execute(_)
	}
	//endregion

	//region configure by name
	def "throws exception for unknown name"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))

		when:
		subject.configure(ownerIdentifier, "foo", entityType, action)

		then:
		def ex = thrown(UnknownDomainObjectException)
		ex.message == "${entityType.simpleName} with name 'foo' and directly owned by ${ownerIdentifier} not found."

		and:
		0 * action.execute(_)
	}

	def "throws exception for known name but wrong type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def identifier = entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		Assume.assumeTrue(identifier instanceof NameAwareDomainObjectIdentifier)
		def name = ((NameAwareDomainObjectIdentifier)identifier).name.toString()

		when:
		subject.configure(ownerIdentifier, name, entityImplementationType, action)

		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == "The domain object '${name}' (${myEntityType.canonicalName}) directly owned by ${ownerIdentifier} is not a subclass of the given type (${entityImplementationType.canonicalName})."

		and:
		0 * action.execute(_)
	}

	def "can configure known name with exact type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def identifier = entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		Assume.assumeTrue(identifier instanceof NameAwareDomainObjectIdentifier)
		def name = ((NameAwareDomainObjectIdentifier)identifier).name.toString()

		and:
		def (_, entity) = entity(identifier)

		when:
		subject.configure(ownerIdentifier, name, myEntityType, action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier, entity)
		then:
		1 * action.execute(entity.get())
	}

	def "can configure known name with super type"() {
		given:
		def subject = newSubject()
		def action = Mock(Action)

		and:
		def identifier = entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier))
		Assume.assumeTrue(identifier instanceof NameAwareDomainObjectIdentifier)
		def name = ((NameAwareDomainObjectIdentifier)identifier).name.toString()

		and:
		def (_, entity) = entity(identifier)

		when:
		subject.configure(ownerIdentifier, name, entityType, action)
		then:
		0 * action.execute(_)

		when:
		entityCreated(identifier, entity)
		then:
		1 * action.execute(entity.get())
	}
	//endregion
}
