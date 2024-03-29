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
import dev.nokee.runtime.base.ArtifactTransformFixture

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static dev.nokee.runtime.base.VerifyTask.artifactType
import static dev.nokee.runtime.base.VerifyTask.verifyTask
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE

class DirectoryArtifactTypeSelectionFunctionalTest extends AbstractGradleSpecification implements ArtifactTransformFixture {
	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
			}
		"""
		executer = executer.withArgument('-i')
	}

	def "can resolve self-resolved directory"() {
		file('test-directory').createDirectory()
		buildFile << verifyTask()
			.that { "configurations.test.${artifactType(DIRECTORY_TYPE)}.singleFile == file('test-directory')" }
			.that { "!transformed(configurations.test.${artifactType(DIRECTORY_TYPE)})" }
		buildFile << '''
			dependencies {
				test files('test-directory')
			}
		'''

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}

	def "can resolve self-resolved compressed file as directory"() {
		file('test/file.txt').createFile()
		compress(file('test'))
		buildFile << verifyTask()
			.that { "configurations.test.${artifactType(DIRECTORY_TYPE)}.singleFile.name == 'test'" }
			.that { "transformed(configurations.test.${artifactType(DIRECTORY_TYPE)})" }
		buildFile << """
			dependencies {
				test files('test.zip')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.output =~ /Transforming( artifact)? test.zip with UnzipTransform/
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
