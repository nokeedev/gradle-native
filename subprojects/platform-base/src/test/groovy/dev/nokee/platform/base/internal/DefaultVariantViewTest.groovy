package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Variant
import groovy.transform.ToString
import org.gradle.api.provider.Provider
import spock.lang.Subject

import javax.inject.Inject

@Subject(DefaultVariantView)
class DefaultVariantViewTest extends AbstractViewTest<Variant> {
	final def backingCollection = objects.domainObjectSet(TestVariant)

	@Override
	def getBackingCollection() {
		return backingCollection
	}

	@Override
	void realizeBackingCollection() {
		backingCollection.iterator().next()
	}

	def createView() {
		return objects.newInstance(DefaultVariantView, Variant, backingCollection, realizeTrigger)
	}

	@Override
	void addToBackingCollection(Provider<Variant> v) {
		backingCollection.addLater(v)
	}

	@Override
	Provider<Variant> getA() {
		return providers.provider { objects.newInstance(TestVariant, 'a') }
	}

	@Override
	Provider<Variant> getB() {
		return providers.provider { objects.newInstance(TestVariant, 'b') }
	}

	@Override
	Provider<Variant> getC() {
		return providers.provider { objects.newInstance(TestChildVariant, 'c') }
	}

	@Override
	Class<TestVariant> getType() {
		return TestVariant
	}

	@Override
	Class<TestChildVariant> getOtherType() {
		return TestChildVariant
	}

	@ToString
	static abstract class TestVariant extends BaseVariant implements Variant, AbstractViewTest.Identifiable {
		private final String identification

		@Inject
		TestVariant(String identification) {
			super('test', DefaultBuildVariant.of())
			this.identification = identification
		}

		@Override
		String getIdentification() {
			return identification
		}
	}

	static abstract class TestChildVariant extends TestVariant {
		@Inject
		TestChildVariant(String identification) {
			super(identification)
		}
	}
}
