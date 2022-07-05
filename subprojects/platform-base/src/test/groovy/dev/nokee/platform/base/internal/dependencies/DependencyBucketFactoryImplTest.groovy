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
package dev.nokee.platform.base.internal.dependencies

import dev.nokee.model.DependencyFactory
import dev.nokee.model.NamedDomainObjectRegistry
import dev.nokee.model.internal.ProjectIdentifier
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject

@Subject(DependencyBucketFactoryImpl)
class DependencyBucketFactoryImplTest extends Specification {
	@Unroll
	def "can create dependency bucket"(bucketType, expectedConfigurationType) {
		given:
		def configurations = rootProject().configurations
		def configurationRegistry = NamedDomainObjectRegistry.of(configurations)
		def subject = new DependencyBucketFactoryImpl(configurationRegistry, Stub(DependencyFactory))
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('foo'), bucketType, ProjectIdentifier.of('root'))

		when:
		def result = subject.create(identifier)

		then:
		if (bucketType == DeclarableDependencyBucket) {
			!result.asConfiguration.canBeConsumed
			!result.asConfiguration.canBeResolved
		} else if (bucketType == ResolvableDependencyBucket) {
			!result.asConfiguration.canBeConsumed
			result.asConfiguration.canBeResolved
		} else if (bucketType == ConsumableDependencyBucket) {
			result.asConfiguration.canBeConsumed
			!result.asConfiguration.canBeResolved
		} else {
			throw new UnsupportedOperationException()
		}

		where:
		bucketType 					| expectedConfigurationType
		DeclarableDependencyBucket 	| ConfigurationBucketType.DECLARABLE
		ResolvableDependencyBucket 	| ConfigurationBucketType.RESOLVABLE
		ConsumableDependencyBucket 	| ConfigurationBucketType.CONSUMABLE
	}
}
