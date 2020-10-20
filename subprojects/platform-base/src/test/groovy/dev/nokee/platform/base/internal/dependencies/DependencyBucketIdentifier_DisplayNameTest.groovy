package dev.nokee.platform.base.internal.dependencies

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
	def "can generate name for single word bucket name owned by a project"(projectName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of(projectName))

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
		identifier.displayName == "${expectedValue} dependencies for project ':root'."

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
		identifier('headerSearchPaths').displayName == "Header search paths dependencies for project ':root'."
		identifier('importSwiftModules').displayName == "Import swift modules dependencies for project ':root'."
		// TODO: Maybe we should capitalize the language name
	}

	def "can generate name for single word bucket name owned by the main component"() {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))

		expect:
		identifier.displayName == "Implementation dependencies for main component."
	}

	@Unroll
	def "can generate name for single word bucket name owned by a non-main component"(componentName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ComponentIdentifier.of(ComponentName.of(componentName), Component, ProjectIdentifier.of('root')))

		expect:
		identifier.displayName == "Implementation dependencies for component '${componentName}'."

		where:
		componentName << ['test', 'integTest', 'uiTest', 'unitTest']
	}

	@Unroll
	def "can generate name for single word bucket name owned by a variant of the main component"(unambiguousVariantName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, VariantIdentifier.of(unambiguousVariantName, Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root'))))

		expect:
		identifier.displayName == "Implementation dependencies for variant '${unambiguousVariantName}'."

		where:
		unambiguousVariantName << ['macosDebug', 'x86-64Debug', 'windowsX86']
	}

	@Unroll
	def "can generate name for single word bucket name owned by a variant of the non-main component"(unambiguousVariantName, componentName) {
		given:
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, VariantIdentifier.of(unambiguousVariantName, Variant, ComponentIdentifier.of(ComponentName.of(componentName), Component, ProjectIdentifier.of('root'))))

		expect:
		identifier.displayName == "Implementation dependencies for variant '${unambiguousVariantName}' of component '${componentName}'."

		where:
		[unambiguousVariantName, componentName] << [['macosDebug', 'x86-64Debug', 'windowsX86'], ['test', 'integTest', 'uiTest', 'unitTest']].combinations()
	}

	def "always capitalize API word"() {
		expect:
		identifier('api').displayName == "API dependencies for project ':root'."
		identifier('apiElements').displayName == "API elements dependencies for project ':root'."
		identifier('jvmApiElements').displayName == "Jvm API elements dependencies for project ':root'."
		// TODO: Should we also capitalize JVM?
	}

	def "does not add the word dependencies to the consumable buckets"() {
		expect:
		identifier('runtimeElements', TestableConsumableBucket).displayName == "Runtime elements for project ':root'."
		identifier('compileElements', TestableConsumableBucket).displayName == "Compile elements for project ':root'."
	}

	def "does not add the word dependencies to the resolvable buckets"() {
		expect:
		identifier('linkLibraries', TestableResolvableBucket).displayName == "Link libraries for project ':root'."
		identifier('runtimeLibraries', TestableResolvableBucket).displayName == "Runtime libraries for project ':root'."
		identifier('headerSearchPaths', TestableResolvableBucket).displayName == "Header search paths for project ':root'."
		identifier('importSwiftModules', TestableResolvableBucket).displayName == "Import swift modules for project ':root'."
	}

	def "uses component display name for single variant"() {
		given:
		def mainOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def testOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of('root')))
		def customDisplayNameOwnerIdentifier = VariantIdentifier.of('', Variant, ComponentIdentifier.builder().withName(ComponentName.of('test')).withProjectIdentifier(ProjectIdentifier.of('root')).withDisplayName('JNI library').withType(Component).build())

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
