package dev.nokee.platform.nativebase.internal

import dev.nokee.platform.base.internal.DefaultBuildVariant
import dev.nokee.platform.base.internal.VariantInternal
import dev.nokee.runtime.core.Coordinate
import dev.nokee.runtime.core.CoordinateAxis
import org.gradle.api.Named
import spock.lang.Specification
import spock.lang.Subject

@Subject(SingleVariantDimensionComparator)
class SingleVariantDimensionComparatorTest extends Specification {
	def dimensionType = CoordinateAxis.of(TestableDimension);
	def delegate = Mock(Comparator)
	def subject = new SingleVariantDimensionComparator(dimensionType, delegate)
	def leftVariant = Mock(VariantInternal)
	def rightVariant = Mock(VariantInternal)

	def "forwards comparision when both variant has a matching dimension value"() {
		given:
		def leftVariantDimension = newDimension()
		def rightVariantDimension = newDimension()

		and:
		leftVariant.buildVariant >> DefaultBuildVariant.of(leftVariantDimension)
		rightVariant.buildVariant >> DefaultBuildVariant.of(rightVariantDimension)

		when:
		subject.compare(leftVariant, rightVariant)

		then:
		1 * delegate.compare(leftVariantDimension, rightVariantDimension)
	}

	private Coordinate newDimension() {
		def result = Mock(TestableDimension) {
			getAxis() >> dimensionType
			getName() >> 'test'
		}
		result.getValue() >> result
		return result
	}

	interface TestableDimension extends Coordinate, Named {}
}
