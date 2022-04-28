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
package dev.nokee.platform.base.internal

import dev.nokee.model.internal.ProjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(BinaryIdentifier)
class BinaryIdentifier_OutputDirectoryBaseTest extends Specification {
	def "can generate output directory base for artifact owned by main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), componentIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main'
	}

	def "can generate output directory base for artifact owned by non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), componentIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test'
	}

	def "can generate output directory base for artifact owned by variant of single-variant main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}]).withVariantDimension({'macos'}, [{'macos'}]).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main'
	}

	def "can generate output directory base for artifact owned by variant of single-variant non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}]).withVariantDimension({'macos'}, [{'macos'}]).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test'
	}


	def "can generate output directory base for artifact owned by variant of multi-variant main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}, {'release'}]).withVariantDimension({'macos'}, [{'macos'}, {'windows'}]).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/main/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/main/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/main/debug/macos'
	}

	def "can generate output directory base for artifact owned by variant of multi-variant non-main component"() {
		given:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), projectIdentifier)
		def variantIdentifier = VariantIdentifier.builder().withVariantDimension({'debug'}, [{'debug'}, {'release'}]).withVariantDimension({'macos'}, [{'macos'}, {'windows'}]).withComponentIdentifier(componentIdentifier).build()
		def binaryIdentifier = BinaryIdentifier.of(BinaryName.of('foo'), variantIdentifier)

		expect:
		binaryIdentifier.getOutputDirectoryBase('objs') == 'objs/test/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('objs/for-test') == 'objs/for-test/test/debug/macos'
		binaryIdentifier.getOutputDirectoryBase('libs') == 'libs/test/debug/macos'
	}
}
