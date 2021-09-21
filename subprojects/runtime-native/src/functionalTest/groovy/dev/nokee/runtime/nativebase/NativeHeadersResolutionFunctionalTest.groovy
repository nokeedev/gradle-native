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
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.runtime.base.ArtifactTransformFixture
import spock.lang.Unroll

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static dev.nokee.runtime.base.VerifyTask.uncompressedFiles
import static dev.nokee.runtime.base.VerifyTask.verifyTask

class NativeHeadersResolutionFunctionalTest extends AbstractGradleSpecification implements ArtifactTransformFixture {
	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
			}
		"""
		executer = executer.withArgument('-i')
	}

	private static TestFile createTestHeaders(TestFile destinationDirectory) {
		def fixture = new CGreeter()
		def includeDirectory = destinationDirectory.createDirectory('includes')
		fixture.publicHeaders.writeToSourceDir(includeDirectory)
		return includeDirectory
	}

	def "can resolve adhoc headers from zip archive"() {
		buildFile << verifyTask()
			.that { "configurations.test.${uncompressedFiles()}.singleFile.name == 'includes'" }
			.that { "transformed(configurations.test.${uncompressedFiles()})" }
		buildFile << """
			dependencies {
				test files('${compress(createTestHeaders(testDirectory)).name}')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.output =~ /Transforming( artifact)? includes.zip with UnzipTransform/
	}

	def "can resolve adhoc header search directories"() {
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('includes')" }
			.that { "!transformed(configurations.test)" }
		buildFile << """
			dependencies {
				test files('${createTestHeaders(testDirectory).name}')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}

	def "can resolve compressed headers from project"() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${compress(createTestHeaders(file('lib'))).name}')) {
					type = 'native-headers-zip'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.test.${uncompressedFiles()}.singleFile.name == 'includes'" }
			.that { "transformed(configurations.test.${uncompressedFiles()})" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.output =~ /Transform(ing artifact)? includes.zip \(project :lib\) with HeadersArchiveToHeaderSearchPath/
	}

	@Unroll
	def "can resolve header search directories from project [#directoryType]"(String directoryType) {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${createTestHeaders(file('lib')).name}')) {
					type = '${directoryType}'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/includes')" }
			.that { "!transformed(configurations.test)" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')

		where:
		directoryType << ['directory', 'native-headers-directory']
	}

	def "can resolve header search directories from project with artifact variant"() {
		settingsFile << '''
			include 'lib'
		'''
		def headersLocation = createTestHeaders(file('lib'))
		file('lib/build.gradle') << """
			def zipTask = tasks.register('zipHeaders', Zip) {
				from('${headersLocation.name}')
				destinationDirectory = buildDir
				archiveBaseName = 'headers'
			}

			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing {
					artifact(file('${headersLocation.name}')) {
						type = 'native-headers-directory'
					}
					variants.create('zip') {
						artifact(zipTask) {
							type = 'native-headers-zip'
						}
					}
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/includes')" }
			.that { "!transformed(configurations.test)" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')
	}

	private static File compress(TestFile srcDir) {
		File zipFile = new File(srcDir.getAbsolutePath() + ".zip")
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))
		srcDir.eachFileRecurse({
			zos.putNextEntry(new ZipEntry(it.path - srcDir.path + (it.directory ? "/" : "")))
			if(it.file) { zos << it.bytes }
			zos.closeEntry()
		})
		zos.close()
		return zipFile
	}
}
