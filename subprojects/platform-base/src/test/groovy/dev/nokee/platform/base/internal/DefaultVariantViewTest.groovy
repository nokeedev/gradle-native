package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Variant
import groovy.transform.ToString
import org.gradle.api.provider.Provider

class DefaultVariantViewTest extends AbstractViewTest<Variant> {
	final def backingCollection = objects.domainObjectSet(TestVariant)

	def getBackingCollection() {
		return backingCollection
	}

	@Override
	Class getViewType() {
		return DefaultVariantView
	}

	@Override
	void addToBackingCollection(Provider<Variant> v) {
		backingCollection.addLater(v)
	}

	@Override
	Provider<Variant> getA() {
		return providers.provider { new TestVariant('a') }
	}

	@Override
	Provider<Variant> getB() {
		return providers.provider { new TestVariant('b') }
	}

	@ToString
	private static class TestVariant implements Variant, AbstractViewTest.Identifiable {
		private final String identification

		TestVariant(String identification) {
			this.identification = identification
		}

		@Override
		String getIdentification() {
			return identification
		}
	}
}
