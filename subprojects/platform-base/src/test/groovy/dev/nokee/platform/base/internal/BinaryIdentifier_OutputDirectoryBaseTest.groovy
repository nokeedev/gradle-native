package dev.nokee.platform.base.internal

import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import spock.lang.Specification
import spock.lang.Subject

@Subject(BinaryIdentifier)
class BinaryIdentifier_OutputDirectoryBaseTest extends Specification {
	def "can generate output directory base for artifact owned by main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, componentIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main'
	}

	def "can generate output directory base for artifact owned by non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, componentIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test'
	}

	def "can generate output directory base for artifact owned by variant of single-variant main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}]).withVariantDimension({'macos'}, [{'macos'}]).withType(Variant).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main'
	}

	def "can generate output directory base for artifact owned by variant of single-variant non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}]).withVariantDimension({'macos'}, [{'macos'}]).withType(Variant).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test'
	}


	def "can generate output directory base for artifact owned by variant of multi-variant main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}, {'release'}]).withVariantDimension({'macos'}, [{'macos'}, {'windows'}]).withType(Variant).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main/debug/macos'
	}

	def "can generate output directory base for artifact owned by variant of multi-variant non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), Component, projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}, {'release'}]).withVariantDimension({'macos'}, [{'macos'}, {'windows'}]).withType(Variant).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), Binary, variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test/debug/macos'
	}
}
