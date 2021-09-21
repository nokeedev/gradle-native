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

import dev.nokee.model.internal.ProjectIdentifier
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName
import static dev.nokee.utils.ConfigurationUtils.configureDescription

@Subject(DependencyBucketFactoryImpl)
class DependencyBucketFactoryImplTest extends Specification {
	@Unroll
	def "can create dependency bucket"(bucketType, expectedConfigurationType) {
		given:
		def configurationRegistry = Mock(ConfigurationBucketRegistry)
		def subject = new DependencyBucketFactoryImpl(configurationRegistry, Stub(DependencyHandler))
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('foo'), bucketType, ProjectIdentifier.of('root'))

		when:
		subject.create(identifier)

		then:
		1 * configurationRegistry.createIfAbsent('foo', expectedConfigurationType, configureDescription(mapDisplayName(identifier))) >> Stub(Configuration)

		where:
		bucketType 					| expectedConfigurationType
		DeclarableDependencyBucket 	| ConfigurationBucketType.DECLARABLE
		ResolvableDependencyBucket 	| ConfigurationBucketType.RESOLVABLE
		ConsumableDependencyBucket 	| ConfigurationBucketType.CONSUMABLE
	}
}
