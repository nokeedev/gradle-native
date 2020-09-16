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
