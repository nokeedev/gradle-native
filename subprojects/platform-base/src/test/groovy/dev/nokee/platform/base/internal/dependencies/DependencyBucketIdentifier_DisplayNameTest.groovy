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
package dev.nokee.platform.base.internal.dependencies

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(DependencyBucketIdentifier)
class DependencyBucketIdentifier_DisplayNameTest extends Specification {
	@Unroll
	def "can generate name for single word bucket name owned by a root project"() {
		given:
		def project = ProjectTestUtils.rootProject()
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of(project))

		expect:
		identifier.displayName == "Implementation dependencies for project ':'."
	}

	@Unroll
	def "can generate name for single word bucket name owned by a project"(projectName) {
		given:
		def rootProject = ProjectTestUtils.rootProject()
		def childProject = ProjectTestUtils.createChildProject(rootProject, projectName)
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of(childProject))

		expect:
		identifier.displayName == "Implementation dependencies for project ':${projectName}'."

		where:
		projectName << ['root', 'foo', 'bar']
	}

	@Unroll
	def "can generate name for two words bucket name owned by a project"(bucketName, expectedValue) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of(bucketName), TestableBucket, ProjectIdentifier.of('root'))

		expect:
		identifier.displayName == "${expectedValue} dependencies for project ':'."

		where:
		bucketName    	  | expectedValue
		'compileOnly' 	  | 'Compile only'
		'linkOnly'    	  | 'Link only'
		'runtimeOnly' 	  | 'Runtime only'
		'runtimeElements' | 'Runtime elements'
		'compileElements' | 'Compile elements'
	}

	def "can generate name for three words bucket name owned by a project"() {
		expect:
		identifier('headerSearchPaths').displayName == "Header search paths dependencies for project ':'."
		identifier('importSwiftModules').displayName == "Import swift modules dependencies for project ':'."
		// TODO: Maybe we should capitalize the language name
	}

	def "can generate name for single word bucket name owned by the main component"() {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))

		expect:
		identifier.displayName == "Implementation dependencies for main component."
	}

	@Unroll
	def "can generate name for single word bucket name owned by a non-main component"(componentName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ComponentIdentifier.of(ComponentName.of(componentName), ProjectIdentifier.of('root')))

		expect:
		identifier.displayName == "Implementation dependencies for component '${componentName}'."

		where:
		componentName << ['test', 'integTest', 'uiTest', 'unitTest']
	}

	@Unroll
	def "can generate name for single word bucket name owned by a variant of the main component"(unambiguousVariantName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, VariantIdentifier.of(unambiguousVariantName, Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of('root'))))

		expect:
		identifier.displayName == "Implementation dependencies for variant '${unambiguousVariantName}'."

		where:
		unambiguousVariantName << ['macosDebug', 'x86-64Debug', 'windowsX86']
	}

	@Unroll
	def "can generate name for single word bucket name owned by a variant of the non-main component"(unambiguousVariantName, componentName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, VariantIdentifier.of(unambiguousVariantName, Variant, ComponentIdentifier.of(ComponentName.of(componentName), ProjectIdentifier.of('root'))))

		expect:
		identifier.displayName == "Implementation dependencies for variant '${unambiguousVariantName}' of component '${componentName}'."

		where:
		[unambiguousVariantName, componentName] << [['macosDebug', 'x86-64Debug', 'windowsX86'], ['test', 'integTest', 'uiTest', 'unitTest']].combinations()
	}

	def "always capitalize API word"() {
		expect:
		identifier('api').displayName == "API dependencies for project ':'."
		identifier('apiElements').displayName == "API elements dependencies for project ':'."
		identifier('jvmApiElements').displayName == "Jvm API elements dependencies for project ':'."
		// TODO: Should we also capitalize JVM?
	}

	def "does not add the word dependencies to the consumable buckets"() {
		expect:
		identifier('runtimeElements', TestableConsumableBucket).displayName == "Runtime elements for project ':'."
		identifier('compileElements', TestableConsumableBucket).displayName == "Compile elements for project ':'."
	}

	def "does not add the word dependencies to the resolvable buckets"() {
		expect:
		identifier('linkLibraries', TestableResolvableBucket).displayName == "Link libraries for project ':'."
		identifier('runtimeLibraries', TestableResolvableBucket).displayName == "Runtime libraries for project ':'."
		identifier('headerSearchPaths', TestableResolvableBucket).displayName == "Header search paths for project ':'."
		identifier('importSwiftModules', TestableResolvableBucket).displayName == "Import swift modules for project ':'."
	}

	def "uses component display name for single variant"() {
		given:
		def mainOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))
		def testOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.of(ComponentName.of('test'), ProjectIdentifier.of('root')))
		def customDisplayNameOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.builder().withName(ComponentName.of('test')).withProjectIdentifier(ProjectIdentifier.of('root')).withDisplayName('JNI library').build())

		expect:
		identifier('implementation', mainOwnerIdentifier).displayName == "Implementation dependencies for main component."
		identifier('implementation', testOwnerIdentifier).displayName == "Implementation dependencies for component 'test'."
		identifier('implementation', customDisplayNameOwnerIdentifier).displayName == "Implementation dependencies for JNI library."
	}

	private static DependencyBucketIdentifier identifier(String name, DomainObjectIdentifierInternal owner) {
		return identifier(name, TestableBucket, owner)
	}

	private static <T extends DependencyBucket> DependencyBucketIdentifier<T> identifier(String name, Class<T> type = TestableBucket, DomainObjectIdentifierInternal owner = ProjectIdentifier.of('root')) {
		return DependencyBucketIdentifier.of(DependencyBucketName.of(name), type, owner)
	}

	interface TestableBucket extends DependencyBucket {}
	interface TestableConsumableBucket extends ConsumableDependencyBucket {}
	interface TestableResolvableBucket extends ResolvableDependencyBucket {}
}
