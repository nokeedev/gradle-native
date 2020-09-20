package dev.nokee.platform.base.internal


import dev.nokee.model.internal.NokeeCollection
import dev.nokee.model.internal.NokeeMap
import dev.nokee.model.internal.Value
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import org.gradle.api.Action

import static dev.nokee.utils.TransformerUtils.configureInPlace

class VariantViewImplTest extends AbstractViewImplTest {
	@Override
	protected newSubject(NokeeCollection collection) {
		return new VariantViewImpl<>(collection)
	}

	@Override
	protected Class<?> getViewImplementationType() {
		return VariantViewImpl
	}

	@Override
	protected def valueOf(String value) {
		return Stub(NokeeMap.Entry) {
			getValue() >> Value.fixed(value)
		}
	}

	@Override
	protected values(Action action) {
		return VariantViewImpl.configureValue(action)
	}

	@Override
	protected byType(Class type) {
		return VariantViewImpl.byType(type)
	}

	def "delegates when element known action to the underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)
		def action = Stub(Action)

		when:
		subject.whenElementKnown(action)

		then:
		1 * collection.whenElementAdded(VariantViewImpl.asKnownVariant(action))
		0 * collection._
	}

	def "can map entry to known variant"() {
		given:
		def action = Mock(Action)

		and:
		def key = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Value.fixed(Stub(Variant))
		def entry = Stub(NokeeMap.Entry) {
			getKey() >> key
			getValue() >> value
		}

		when:
		VariantViewImpl.asKnownVariant(action).execute(Value.fixed(entry))

		then:
		1 * action.execute(KnownVariant.of(key, value))
		0 * action._
	}

	def "can filter by variant type"() {
		given:
		def entry1 = Stub(NokeeMap.Entry) {
			getKey() >> VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
			getValue() >> Value.fixed(Stub(Variant))
		}
		def entry2 = Stub(NokeeMap.Entry) {
			getKey() >> VariantIdentifier.of('debug', MyVariant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
			getValue() >> Value.fixed(Stub(MyVariant))
		}

		expect:
		VariantViewImpl.byType(Variant).isSatisfiedBy(entry1)
		VariantViewImpl.byType(Variant).isSatisfiedBy(entry2)

		and:
		!VariantViewImpl.byType(MyVariant).isSatisfiedBy(entry1)
		VariantViewImpl.byType(MyVariant).isSatisfiedBy(entry2)
	}

	def "can configure entry value"() {
		given:
		def action = Stub(Action)

		and:
		def key = VariantIdentifier.of('debug', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def value = Mock(Value)
		def entry = Stub(NokeeMap.Entry) {
			getKey() >> key
			getValue() >> value
		}

		when:
		VariantViewImpl.configureValue(action).execute(entry)

		then:
		1 * value.mapInPlace(configureInPlace(action))
		0 * value._
	}

	interface MyVariant extends Variant {}
}
