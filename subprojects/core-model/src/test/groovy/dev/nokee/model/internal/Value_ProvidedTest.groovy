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

import dev.nokee.utils.ProviderUtils
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Transformer
import org.gradle.api.internal.provider.ProviderInternal
import spock.lang.Subject

@Subject(Value)
class Value_ProvidedTest extends Value_AbstractTest {

	// Approximation of the NamedDomainObjectProvider implementation
	def <T> NamedDomainObjectProvider<T> providerOf(T v) {
		def delegate = ProviderUtils.fixed(v)
		def actions = []
		T value = null
		def getter = {
			if (value == null) {
				def result = delegate.get()
				actions.each {
					it.execute(result)
				}
				value = result
			}
			return value;
		}
		def result = Stub(TypeAwareNamedDomainObjectProvider) {
			get() >> { getter() }
			isPresent() >> { true }
			configure(_) >> { Action action ->
				if (value == null) {
					actions << action
				} else {
					action.execute(value)
				}
			}
			getType() >> { v.getClass() }
			getOrNull() >> { getter() }
			map(_ as Transformer) >> { args -> ProviderUtils.supplied(getter).map(args[0]) }
			flatMap(_ as Transformer) >> { args -> ProviderUtils.supplied(getter).flatMap(args[0]) }
		}

		return result
	}

	@Override
	def <T> Value<T> newSubject(T value) {
		return Value.provided(providerOf(value))
	}

	def "delegate in-place mapping to provider configure method"() {
		given:
		def provider = Mock(NamedDomainObjectProvider)
		def subject = Value.provided(provider)
		def mapper = Mock(Transformer)
		def capturedConfigureAction = null

		when:
		subject.mapInPlace(mapper)
		then:
		1 * provider.configure(_) >> { args -> capturedConfigureAction = args[0] }
		capturedConfigureAction != null

		when:
		capturedConfigureAction.execute(42)
		then:
		1 * mapper.transform(42)
	}

	def "querying the type does not resolve the provider"() {
		given:
		def provider = Mock(TypeAwareNamedDomainObjectProvider) {
			getType() >> Object
		}
		def subject = Value.provided(provider)

		when:
		subject.getType()

		then:
		0 * provider.get()
	}

	interface TypeAwareNamedDomainObjectProvider<T> extends NamedDomainObjectProvider<T>, ProviderInternal<T> {}
}
