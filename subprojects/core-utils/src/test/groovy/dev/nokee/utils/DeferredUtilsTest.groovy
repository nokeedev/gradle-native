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
package dev.nokee.utils


import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory
import static dev.nokee.utils.DeferredUtils.*

@Subject(DeferredUtils)
class DeferredUtilsTest extends DeferredUtils_BaseSpec {
	def "can unpack provider types"() {
		expect:
		unpack(propertyOf('foo')) == 'foo'
		unpack(providerOf('foo')) == 'foo'
		unpack(closureOf('foo')) == 'foo'
		unpack(supplierOf('foo')) == 'foo'
		unpack(kotlinFunctionOf('foo')) == 'foo'
		unpack(callableOf('foo')) == 'foo'
	}

	def "can detect deferrable types"() {
		expect:
		// are
		isDeferred(propertyOf(Object, throwIfResolved()))
		isDeferred(providerOf(throwIfResolved()))
		isDeferred(closureOf(throwIfResolved()))
		isDeferred(supplierOf(throwIfResolved()))
		isDeferred(kotlinFunctionOf(throwIfResolved()))
		isDeferred(callableOf(throwIfResolved()))

		// aren't
		!isDeferred('foo')
	}

	def "can detect nestable deferrable types"() {
		expect:
		// are
		isNestableDeferred(closureOf(throwIfResolved()))
		isNestableDeferred(supplierOf(throwIfResolved()))
		isNestableDeferred(kotlinFunctionOf(throwIfResolved()))
		isNestableDeferred(callableOf(throwIfResolved()))

		// aren't
		!isNestableDeferred(propertyOf(Object, throwIfResolved()))
		!isNestableDeferred(providerOf(throwIfResolved()))
		!isDeferred('foo')
	}

	@Unroll
	def "can realize deferred collection"(collectionFactoryMethod) {
		given:
		def collection = objectFactory()."${collectionFactoryMethod}"(String)

		and:
		def realizedElements = []
		collection.configureEach {
			realizedElements << it
		}

		and:
		collection.addLater(providerOf(namedElement('foo')))
		collection.addLater(providerOf(namedElement('bar')))

		when:
		assert realizedElements == []
		realize(collection)

		then:
		realizedElements*.name as Set == ['foo', 'bar'] as Set

		where:
		collectionFactoryMethod << ['domainObjectContainer', 'domainObjectSet', 'namedDomainObjectList', 'namedDomainObjectSet']
	}

	@Unroll
	def "can realize empty deferred collection"(collectionFactoryMethod) {
		given:
		def collection = objectFactory()."${collectionFactoryMethod}"(String)

		when:
		realize(collection)

		then:
		noExceptionThrown()

		where:
		collectionFactoryMethod << ['domainObjectContainer', 'domainObjectSet', 'namedDomainObjectList', 'namedDomainObjectSet']
	}

//	def "can unpack list to File"() {
//		given:
//		def deferred = []
//
//		expect:
//
//	}
}
