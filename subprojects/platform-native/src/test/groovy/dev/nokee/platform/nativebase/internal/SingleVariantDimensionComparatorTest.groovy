package dev.nokee.platform.nativebase.internal

import dev.nokee.platform.base.internal.DefaultBuildVariant
import dev.nokee.platform.base.internal.VariantInternal
import dev.nokee.runtime.base.internal.DefaultDimensionType
import dev.nokee.runtime.base.internal.Dimension
import spock.lang.Specification
import spock.lang.Subject

@Subject(SingleVariantDimensionComparator)
class SingleVariantDimensionComparatorTest extends Specification {
	def dimensionType = new DefaultDimensionType()
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

	def "selects left variant when it's the only variant with the dimension value"() {
		given:
		leftVariant.buildVariant >> DefaultBuildVariant.of(newDimension())
		rightVariant.buildVariant >> DefaultBuildVariant.of()

		when:
		def result = subject.compare(leftVariant, rightVariant)

		then:
		0 * delegate.compare(_, _)

		and:
		result == -1
	}

	def "selects right variant when it's the only variant with the dimension value"() {
		given:
		leftVariant.buildVariant >> DefaultBuildVariant.of()
		rightVariant.buildVariant >> DefaultBuildVariant.of(newDimension())

		when:
		def result = subject.compare(leftVariant, rightVariant)

		then:
		0 * delegate.compare(_, _)

		and:
		result == 1
	}

	private Dimension newDimension() {
		return Mock(TestableDimension) {
			getType() >> dimensionType
		}
	}

	interface TestableDimension extends Dimension {}
}
