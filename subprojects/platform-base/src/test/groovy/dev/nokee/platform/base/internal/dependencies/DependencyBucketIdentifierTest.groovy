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
import dev.nokee.model.internal.names.ElementName
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(DependencyBucketIdentifier)
class DependencyBucketIdentifierTest extends Specification {
	def "can create identifier owned by a project using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def bucketName = ElementName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, projectIdentifier)

		then:
		identifier.name == bucketName
		identifier.ownerIdentifier == projectIdentifier
	}

	def "can create identifier owned by a component using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def bucketName = ElementName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, componentIdentifier)

		then:
		identifier.name == bucketName
		identifier.ownerIdentifier == componentIdentifier
	}

	def "can create identifier owned by a variant using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', componentIdentifier)
		def bucketName = ElementName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, variantIdentifier)

		then:
		identifier.name == bucketName
		identifier.ownerIdentifier == variantIdentifier
	}

	def "throws exception when dependency bucket name is null"() {
		when:
		DependencyBucketIdentifier.of(null, TestableBucket, ProjectIdentifier.of('root'))

		then:
		thrown(NullPointerException)
	}

	def "throws exception when owner is null"() {
		when:
		DependencyBucketIdentifier.of('implementation', null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency identifier because the owner identifier is null.'
	}

	interface TestableBucket extends DependencyBucket {}
}
