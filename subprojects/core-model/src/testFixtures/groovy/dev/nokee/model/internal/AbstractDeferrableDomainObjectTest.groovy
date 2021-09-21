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
import org.gradle.api.Transformer

abstract class AbstractDeferrableDomainObjectTest<T> extends DomainObjectSpec<T> {
	protected abstract <S extends T> Object newSubject(TypeAwareDomainObjectIdentifier<S> identifier)

	def "equals two deferrable entity of the same identifier"() {
		given:
		def identifier1 = entityDiscovered(entityIdentifier(ownerIdentifier))
		def identifier2 = entityDiscovered(entityIdentifier(ownerIdentifier))

		expect:
		newSubject(identifier1) == newSubject(identifier1)
		newSubject(identifier2) == newSubject(identifier2)

		and:
		newSubject(identifier1) != newSubject(identifier2)
	}

	def "throws exception when identifier is null"() {
		when:
		newSubject(null)

		then:
		thrown(NullPointerException)
	}

	def "can configuration deferrable entity"() {
		given:
		def (identifier, entity) = entity(entityDiscovered(entityIdentifier(ownerIdentifier)))
		def subject = newSubject(identifier)

		and:
		def action = Mock(Action)

		when:
		subject.configure(action)
		entityCreated(identifier, entity)

		then:
		1 * action.execute({ it == entity.get() })
	}

	def "can map the deferrable entity"() {
		given:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def subject = newSubject(identifier)

		and:
		def transformer = Mock(Transformer)

		when:
		def result = subject.map(transformer)
		then:
		0 * transformer.transform(_)

		when:
		result.get()
		then:
		1 * transformer.transform({ it == entity }) >> { args -> return args[0] }
	}

	def "can flat map the deferrable entity"() {
		given:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def subject = newSubject(identifier)

		and:
		def transformer = Mock(Transformer)

		when:
		def result = subject.flatMap(transformer)
		then:
		0 * transformer.transform(_)

		when:
		result.get()
		then:
		1 * transformer.transform({ it == entity }) >> { args -> return providerFactory.provider { args[0] } }
	}

	def "can query the identifier of the deferrable entity"() {
		given:
		def identifier = entityDiscovered(entityIdentifier(ownerIdentifier))
		def subject = newSubject(identifier)

		expect:
		subject.getIdentifier() == identifier
	}
}
