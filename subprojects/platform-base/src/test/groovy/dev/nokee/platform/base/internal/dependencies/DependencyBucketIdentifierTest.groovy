package dev.nokee.platform.base.internal.dependencies

import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(DependencyBucketIdentifier)
class DependencyBucketIdentifierTest extends Specification {
	def "can create identifier owned by a project using factory method"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
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
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
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
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
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
		def projectIdentifier = ProjectIdentifier.of('root')

		expect:
		identifier('implementation', projectIdentifier).configurationName == 'implementation'
		identifier('compileOnly', projectIdentifier).configurationName == 'compileOnly'
		identifier('headerSearchPaths', projectIdentifier).configurationName == 'headerSearchPaths'
	}

	def "configuration name for main component owned identifier is the same as bucket name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)

		expect:
		identifier('implementation', componentIdentifier).configurationName == 'implementation'
		identifier('compileOnly', componentIdentifier).configurationName == 'compileOnly'
		identifier('headerSearchPaths', componentIdentifier).configurationName == 'headerSearchPaths'
	}

	def "configuration name for non-main component owned identifier starts with component name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)

		expect:
		identifier('implementation', componentIdentifier).configurationName == 'testImplementation'
		identifier('compileOnly', componentIdentifier).configurationName == 'testCompileOnly'
		identifier('headerSearchPaths', componentIdentifier).configurationName == 'testHeaderSearchPaths'
	}

	def "configuration name for variant owned identifier of main component starts with unambiguous variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', Variant, componentIdentifier)

		expect:
		identifier('implementation', variantIdentifier).configurationName == 'macosDebugImplementation'
		identifier('compileOnly', variantIdentifier).configurationName == 'macosDebugCompileOnly'
		identifier('headerSearchPaths', variantIdentifier).configurationName == 'macosDebugHeaderSearchPaths'
	}

	def "configuration name for variant owned identifier of non-main component starts with component name followed by unambiguous variant name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('macosDebug', Variant, componentIdentifier)

		expect:
		identifier('implementation', variantIdentifier).configurationName == 'testMacosDebugImplementation'
		identifier('compileOnly', variantIdentifier).configurationName == 'testMacosDebugCompileOnly'
		identifier('headerSearchPaths', variantIdentifier).configurationName == 'testMacosDebugHeaderSearchPaths'
	}

	def "configuration name for unique variant owned identifier of main component is the same as bucket name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', Variant, componentIdentifier)

		expect:
		identifier('implementation', variantIdentifier).configurationName == 'implementation'
		identifier('compileOnly', variantIdentifier).configurationName == 'compileOnly'
		identifier('headerSearchPaths', variantIdentifier).configurationName == 'headerSearchPaths'
	}

	def "configuration name for unique variant owned identifier of non-main component starts with component name"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.of('', Variant, componentIdentifier)

		expect:
		identifier('implementation', variantIdentifier).configurationName == 'testImplementation'
		identifier('compileOnly', variantIdentifier).configurationName == 'testCompileOnly'
		identifier('headerSearchPaths', variantIdentifier).configurationName == 'testHeaderSearchPaths'
	}

	def "throws exception when dependency bucket name is null"() {
		when:
		DependencyBucketIdentifier.of(null, TestableBucket, ProjectIdentifier.of('root'))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency identifier because the bucket name is null.'
	}

	def "throws exception when bucket type is null"() {
		when:
		DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), null, ProjectIdentifier.of('root'))

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Cannot construct a dependency identifier because the bucket type is null.'
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

	private static DependencyBucketIdentifier identifier(String name, DomainObjectIdentifierInternal owner) {
		return DependencyBucketIdentifier.of(DependencyBucketName.of(name), DependencyBucketIdentifierTest.TestableBucket, owner)
	}

	interface TestableBucket extends DependencyBucket {}
}
