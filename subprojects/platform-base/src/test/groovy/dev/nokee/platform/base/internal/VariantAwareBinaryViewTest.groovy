package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantView
import groovy.transform.ToString
import org.gradle.api.provider.Provider
import spock.lang.Subject

import javax.inject.Inject

@Subject(VariantAwareBinaryView)
class VariantAwareBinaryViewTest extends AbstractViewTest<Binary> {
	final TestVariant variant = objects.newInstance(TestVariant)
	final def variantCollection = objects.domainObjectSet(TestVariant)
	VariantView<TestVariant> variants = objects.newInstance(DefaultVariantView, TestVariant, variantCollection, realizeTrigger)

	def setup() {
		variantCollection.add(variant)
	}

	@Override
	def getBackingCollection() {
		return variant.binaryCollection
	}

	@Override
	void realizeBackingCollection() {
		variant.binaryCollection.iterator().next()
	}

	@Override
	def createView() {
		return objects.newInstance(VariantAwareBinaryView, Binary, variants)
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
	Provider<Binary> getC() {
		return providers.provider { new TestChildBinary('c') }
	}

	@Override
	Class<TestBinary> getType() {
		return TestBinary
	}

	@Override
	Class<TestChildBinary> getOtherType() {
		return TestChildBinary
	}

	@Override
	void addToBackingCollection(Provider<Binary> v) {
		variant.binaryCollection.addLater(v)
	}

	static abstract class TestVariant extends BaseVariant implements Variant {
		@Inject
		TestVariant() {
			super('test', DefaultBuildVariant.of())
		}
	}

	@ToString
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

	static class TestChildBinary extends TestBinary {
		TestChildBinary(String identification) {
			super(identification)
		}
	}
}
