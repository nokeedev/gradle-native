/*
 * Copyright 2020-2021 the original author or authors.
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
