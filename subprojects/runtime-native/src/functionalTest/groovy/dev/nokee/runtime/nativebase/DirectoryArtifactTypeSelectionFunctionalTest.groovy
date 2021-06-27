package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class DirectoryArtifactTypeSelectionFunctionalTest extends AbstractGradleSpecification {
	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
			}

			def verifyTask = tasks.register('verify') {
				ext.resolvedFiles = configurations.test.incoming.artifactView {
					attributes {
						attribute(Attribute.of('artifactType', String), ArtifactTypeDefinition.DIRECTORY_TYPE)
					}
				}.files
			}
		"""
	}

	def "can resolve self-resolved directory"() {
		file('test-directory').createDirectory()
		buildFile << '''
			dependencies {
				test files('test-directory')
			}

			verifyTask.configure {
				doLast {
					assert resolvedFiles.singleFile.name == 'test-directory'
				}
			}
		'''

		expect:
		succeeds('verify')
	}

	def "fails when resolving self-resolved file"() {
		file('test-file.zip').createFile()
		buildFile << '''
			dependencies {
				test files('test-file.zip')
			}

			verifyTask.configure {
				doLast {
					resolvedFiles.singleFile
				}
			}
		'''

		expect:
		def failure = fails('verify')
		failure.assertOutputContains("Expected configuration ':test' files to contain exactly one file, however, it contains no files.")
	}
}
