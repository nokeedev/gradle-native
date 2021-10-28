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

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.VariantIdentifier
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.configurationName

@Subject(DependencyBucketIdentifier)
class DependencyBucketIdentifierTest extends Specification {
	def "can create identifier owned by a project using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def bucketName = DependencyBucketName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, projectIdentifier)

		then:
		identifier.name == bucketName
		identifier.type == TestableBucket
		identifier.ownerIdentifier == projectIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == projectIdentifier
	}

	def "can create identifier owned by a component using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def bucketName = DependencyBucketName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, componentIdentifier)

		then:
		identifier.name == bucketName
		identifier.type == TestableBucket
		identifier.ownerIdentifier == componentIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == componentIdentifier
	}

	def "can create identifier owned by a variant using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('debug', Variant, componentIdentifier)
		def bucketName = DependencyBucketName.of('implementation')

		when:
		def identifier = DependencyBucketIdentifier.of(bucketName, TestableBucket, variantIdentifier)

		then:
		identifier.name == bucketName
		identifier.type == TestableBucket
		identifier.ownerIdentifier == variantIdentifier
		identifier.parentIdentifier.present
		identifier.parentIdentifier.get() == variantIdentifier
	}

	def "configuration name for project owned identifier is the same as bucket name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()

		expect:
		configurationName(identifier('implementation', projectIdentifier)) == 'implementation'
		configurationName(identifier('compileOnly', projectIdentifier)) == 'compileOnly'
		configurationName(identifier('headerSearchPaths', projectIdentifier)) == 'headerSearchPaths'
	}

	def "configuration name for main component owned identifier is the same as bucket name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)

		expect:
		configurationName(identifier('implementation', componentIdentifier)) == 'implementation'
		configurationName(identifier('compileOnly', componentIdentifier)) == 'compileOnly'
		configurationName(identifier('headerSearchPaths', componentIdentifier)) == 'headerSearchPaths'
	}

	def "configuration name for non-main component owned identifier starts with component name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)

		expect:
		configurationName(identifier('implementation', componentIdentifier)) == 'testImplementation'
		configurationName(identifier('compileOnly', componentIdentifier)) == 'testCompileOnly'
		configurationName(identifier('headerSearchPaths', componentIdentifier)) == 'testHeaderSearchPaths'
	}

	def "configuration name for variant owned identifier of main component starts with unambiguous variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', Variant, componentIdentifier)

		expect:
		configurationName(identifier('implementation', variantIdentifier)) == 'macosDebugImplementation'
		configurationName(identifier('compileOnly', variantIdentifier)) == 'macosDebugCompileOnly'
		configurationName(identifier('headerSearchPaths', variantIdentifier)) == 'macosDebugHeaderSearchPaths'
	}

	def "configuration name for variant owned identifier of non-main component starts with component name followed by unambiguous variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', Variant, componentIdentifier)

		expect:
		configurationName(identifier('implementation', variantIdentifier)) == 'testMacosDebugImplementation'
		configurationName(identifier('compileOnly', variantIdentifier)) == 'testMacosDebugCompileOnly'
		configurationName(identifier('headerSearchPaths', variantIdentifier)) == 'testMacosDebugHeaderSearchPaths'
	}

	def "configuration name for unique variant owned identifier of main component is the same as bucket name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', Variant, componentIdentifier)

		expect:
		configurationName(identifier('implementation', variantIdentifier)) == 'implementation'
		configurationName(identifier('compileOnly', variantIdentifier)) == 'compileOnly'
		configurationName(identifier('headerSearchPaths', variantIdentifier)) == 'headerSearchPaths'
	}

	def "configuration name for unique variant owned identifier of non-main component starts with component name"() {
		given:
		def projectIdentifier = ProjectIdentifier.ofRootProject()
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', Variant, componentIdentifier)

		expect:
		configurationName(identifier('implementation', variantIdentifier)) == 'testImplementation'
		configurationName(identifier('compileOnly', variantIdentifier)) == 'testCompileOnly'
		configurationName(identifier('headerSearchPaths', variantIdentifier)) == 'testHeaderSearchPaths'
	}

	def "throws exception when dependency bucket name is null"() {
		when:
		DependencyBucketIdentifier.of(null, TestableBucket, ProjectIdentifier.of('root'))

		then:
		thrown(NullPointerException)
	}

	def "throws exception when bucket type is null"() {
		when:
		DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), null, ProjectIdentifier.ofRootProject())

		then:
		thrown(NullPointerException)
	}

	def "throws exception when owner is null"() {
		when:
		DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency identifier because the owner identifier is null.'
	}

	def "throws exception when owner is not a project, component or variant"() {
		when:
		DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, Mock(DomainObjectIdentifierInternal))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency identifier because the owner identifier is invalid, only ProjectIdentifier, ComponentIdentifier and VariantIdentifier are accepted.'
	}

	private static DependencyBucketIdentifier identifier(String name, DomainObjectIdentifier owner) {
		return DependencyBucketIdentifier.of(DependencyBucketName.of(name), DependencyBucketIdentifierTest.TestableBucket, owner)
	}

	interface TestableBucket extends DependencyBucket {}
}
