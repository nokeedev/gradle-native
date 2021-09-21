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
package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Unroll

class XcodeIdeIndexerTargetsFunctionalTest extends AbstractGradleSpecification implements XcodeIdeFixture {
	@Unroll
	def "creates an indexer target for known product types (e.g. #productType)"(productType) {
		given:
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo', productType)

		when:
		succeeds('xcode')

		then:
		xcodeProject('foo').targets*.name == ['Foo', '__indexer_Foo']
		xcodeProject('foo').getTargetByName('__indexer_Foo').productType == 'dev.nokee.product-type.indexer'
		// TODO: Ensure each product type has the right build settings configured

		where:
		productType << (XcodeIdeProductTypes.getKnownValues() - [XcodeIdeProductTypes.UNIT_TEST, XcodeIdeProductTypes.UI_TEST])
	}

	def "does not create schemes for indexer target"() {
		given:
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		succeeds('xcode')

		then:
		xcodeProject('foo').schemes*.name == ['Foo']
	}
}
