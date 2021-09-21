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

import com.google.common.collect.ImmutableSet
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.DomainObjectView
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.specs.Spec

abstract class AbstractDomainObjectViewTest<T> extends AbstractDomainObjectCollectionTest<T> {
	protected DomainObjectView<T> newSubject() {
		return newSubject(ownerIdentifier)
	}

	protected DomainObjectView<T> newSubject(DomainObjectIdentifier owner) {
		return newEntityViewFactory().create(owner)
	}

	protected abstract Class<T> getMyEntityType()

	protected abstract Class<T> getMyEntityChildType()

	def "can create view"() {
		when:
		newSubject()

		then:
		noExceptionThrown()
	}

	def "throw exception when owner is null"() {
		when:
		newSubject(null)

		then:
		thrown(NullPointerException)
	}

	//region withType(Class)
	def "can create sub-view by type"() {
		when:
		def subject = newSubject().withType(myEntityType)

		then:
		noExceptionThrown()
		subject != null
	}

	def "can configure each entities in sub-view"() {
		given:
		def subject = newSubject().withType(myEntityType)
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(action)
		then:
		1 * action.execute({ it == entity1.get() })

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * action.execute({ it == entity2.get() })
	}

	def "can configure each entities by type in sub-view"() {
		given:
		def subject = newSubject().withType(myEntityType)
		def action = Mock(Action)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityChildType, ownerIdentifier)))
		def (identifier3, entity3) = entity(entityDiscovered(entityIdentifier(myEntityChildType, ownerIdentifier)))

		and:
		entityCreated(identifier1, entity1)
		entityCreated(identifier2, entity2)

		when:
		subject.configureEach(myEntityChildType, action)
		then:
		1 * action.execute({ it == entity2.get() })

		when:
		entityCreated(identifier3, entity3)
		then:
		1 * action.execute({ it == entity3.get() })
	}

	def "can configure each entities by spec in sub-view"() {
		given:
		def subject = newSubject().withType(myEntityType)
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityType, ownerIdentifier)))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(spec, action)
		then:
		1 * spec.isSatisfiedBy({ it == entity1.get() }) >> false
		0 * action.execute(_)

		when:
		entityCreated(identifier2, entity2)
		then:
		1 * spec.isSatisfiedBy({ it == entity2.get() }) >> true
		1 * action.execute({ it == entity2.get() })
	}

	def "does not configure entities of different owner in sub-view"() {
		given:
		def subject = newSubject().withType(myEntityType)
		def action = Mock(Action)
		def spec = Mock(Spec)

		and:
		def (identifier1, entity1) = entity(entityDiscovered(entityIdentifier(myEntityChildType, ownerIdentifier())))
		def (identifier2, entity2) = entity(entityDiscovered(entityIdentifier(myEntityChildType, ownerIdentifier())))

		and:
		entityCreated(identifier1, entity1)

		when:
		subject.configureEach(action)
		subject.configureEach(myEntityChildType, action)
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

	//region filter(Spec)
	def "can filter the element of the view lazily"() {
		given:
		def subject = newSubject()
		def spec = Mock(Spec)

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def provider = subject.filter(spec)
		then:
		0 * realizedSubscriber.handle(_)
		0 * spec.isSatisfiedBy(_)
		and:
		provider != null

		when:
		def result = provider.get()
		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		1 * spec.isSatisfiedBy(entity1) >> true
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier2, entity2))
		1 * spec.isSatisfiedBy(entity2) >> false
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier3, entity3))
		1 * spec.isSatisfiedBy(entity3) >> true
		and:
		result == [entity1, entity3]
	}
	//endregion

	//region get()
	def "can get the elements for the view"() {
		given:
		def subject = newSubject()

		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def result = subject.get()

		then:
		result == [entity1, entity2, entity3] as Set
		result instanceof ImmutableSet
	}

	def "can get the elements from the view in the order they were discovered"() {
		given:
		def subject = newSubject()

		and:
		def identifier1 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def identifier2 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def identifier3 = entityDiscovered(entityIdentifier(ownerIdentifier))

		and:
		def (i2, entity2) = entityCreated(entity(identifier2))
		def (i1, entity1) = entityCreated(entity(identifier1))
		def (i3, entity3) = entityCreated(entity(identifier3))

		expect:
		subject.get() as List == [entity1, entity2, entity3]
	}

	def "realize discovered from the view when getting"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		and: 'different owner'
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier()))))

		when:
		subject.get()

		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier2, entity2))
	}
	//endregion

	//region getElements()
	def "can get the elements of the view lazily"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def provider = subject.getElements()
		then:
		0 * realizedSubscriber.handle(_)
		provider != null

		when:
		def result = provider.get()
		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier2, entity2))
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier3, entity3))
		and:
		result == [entity1, entity2, entity3] as Set
		result as List == [entity1, entity2, entity3]
	}
	//endregion

	//region flatMap(Transformer)
	def "can flat map the element of the underlying collection lazily"() {
		given:
		def subject = newSubject()
		def mapper = Mock(Transformer)

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def provider = subject.flatMap(mapper)
		then:
		0 * realizedSubscriber.handle(_)
		0 * mapper.transform(_)
		provider != null

		when:
		def result = provider.get()
		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		1 * mapper.transform(entity1) >> ['e1-1', 'e1-2']
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier2, entity2))
		1 * mapper.transform(entity2) >> ['e2-1', 'e2-2']
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier3, entity3))
		1 * mapper.transform(entity3) >> ['e3-1', 'e3-2']
		and:
		result == ['e1-1', 'e1-2', 'e2-1', 'e2-2', 'e3-1', 'e3-2']
	}
	//endregion

	//region flatMap(Transformer)
	def "can map the element of the underlying collection lazily"() {
		given:
		def subject = newSubject()
		def mapper = Mock(Transformer)

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def provider = subject.map(mapper)
		then:
		0 * realizedSubscriber.handle(_)
		0 * mapper.transform(_)
		provider != null

		when:
		def result = provider.get()
		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		1 * mapper.transform(entity1) >> 'e1'
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier2, entity2))
		1 * mapper.transform(entity2) >> 'e2'
		and:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier3, entity3))
		1 * mapper.transform(entity3) >> 'e3'
		and:
		result == ['e1', 'e2', 'e3']
	}
	//endregion
}
