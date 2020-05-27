package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Binary
import org.gradle.api.provider.Provider

class DefaultBinaryViewTest extends AbstractViewTest<Binary> {
	final def backingCollection = objects.domainObjectSet(TestBinary)

	def getBackingCollection() {
		return backingCollection
	}

	@Override
	Class getViewType() {
		return DefaultBinaryView
	}

	@Override
	Provider<Binary> getA() {
		return providers.provider { new TestBinary('a') }
	}

	@Override
	Provider<Binary> getB() {
		return providers.provider { new TestBinary('b') }
	}

	@Override
	void addToBackingCollection(Provider<Binary> v) {
		backingCollection.addLater(v)
	}

	static class TestBinary implements Binary, AbstractViewTest.Identifiable {
		private final String identification

		TestBinary(String identification) {
			this.identification = identification
		}

		@Override
		String getIdentification() {
			return identification
		}
	}
}
