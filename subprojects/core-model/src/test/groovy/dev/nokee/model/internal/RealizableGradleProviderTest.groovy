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

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

@Subject(RealizableGradleProvider)
class RealizableGradleProviderTest extends Specification {
	protected RealizableGradleProvider newSubject(Provider provider) {
		return new RealizableGradleProvider(Stub(DomainObjectIdentifier), provider, Stub(DomainObjectEventPublisher))
	}

	protected RealizableGradleProvider newSubject(Provider provider, DomainObjectIdentifier identifier) {
		return new RealizableGradleProvider(identifier, provider, Stub(DomainObjectEventPublisher))
	}

	def "can realize Gradle provider"() {
		given:
		def provider = Mock(Provider)
		def subject = newSubject(provider)

		when:
		subject.realize()

		then:
		1 * provider.get()
		0 * _
	}

	def "can compare realizable Gradle provider"() {
		given:
		def identifier1 = Stub(DomainObjectIdentifier)
		def provider1 = Stub(Provider)

		and:
		def identifier2 = Stub(DomainObjectIdentifier)
		def provider2 = Stub(Provider)

		expect:
		newSubject(provider1, identifier1) == newSubject(provider1, identifier1)
		newSubject(provider2, identifier2) == newSubject(provider2, identifier2)

		and:
		newSubject(provider1, identifier1) != newSubject(provider2, identifier2)
	}

	def "publishes an entity created event upon realization"() {
		given:
		def eventPublisher = Mock(DomainObjectEventPublisher)
		def entityIdentifier = Stub(DomainObjectIdentifier)
		def entity = new Object()
		def provider = Stub(Provider) {
			get() >> entity
		}
		def subject = new RealizableGradleProvider(entityIdentifier, provider, eventPublisher)

		when:
		subject.realize()

		then:
		1 * eventPublisher.publish(new DomainObjectCreated(entityIdentifier, entity))
	}
}
