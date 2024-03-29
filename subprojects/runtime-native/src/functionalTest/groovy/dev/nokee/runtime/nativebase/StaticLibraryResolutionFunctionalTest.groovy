/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.api.attributes.Usage
import spock.lang.Unroll

import static dev.nokee.runtime.base.VerifyTask.*

class StaticLibraryResolutionFunctionalTest extends AbstractGradleSpecification {
	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = false
			}

			configurations.create('testLink') {
				extendsFrom(configurations.test)
				canBeConsumed = false
				canBeResolved = true
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.NATIVE_LINK))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
			}

			configurations.create('testRuntime') {
				extendsFrom(configurations.test)
				canBeConsumed = false
				canBeResolved = true
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.NATIVE_RUNTIME))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
			}
		"""
		executer = executer.withArgument('-i')
	}

	@Unroll
	def "can resolve adhoc static library file only for link usage [#ext]"(String ext) {
		def staticLib = file("libtest.${ext}").createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "configurations.testLink.${artifactType(ext)}.singleFile == file('${file("libtest.${ext}")}')" }
			.that { "configurations.testRuntime.${artifactType(ext)}.empty" }
		buildFile << """
			dependencies {
				test files('${staticLib.name}')
			}
		"""

		expect:
		succeeds('verify')

		where:
		ext << ['lib', 'a']
	}

	@Unroll
	def "can resolve static library from remote project only for link usage [#ext]"(String ext) {
		def staticLib = file("lib/test.${ext}").createFile()
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_LINK}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.LINK_ARCHIVE))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${staticLib.name}')) {
					type = '${ext}'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.testLink.${allFiles()}.singleFile == file('${file("lib/test.${ext}")}')" }
			.that { "configurations.testRuntime.${allFiles()}.empty" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')

		where:
		ext << ['lib', 'a']
	}
}
