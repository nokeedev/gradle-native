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
				attributes {
					attribute(Attribute.of('artifactType', String), ArtifactTypeDefinition.DIRECTORY_TYPE)
				}
			}

			def verifyTask = tasks.register('verify')
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
					assert configurations.test.singleFile.name == 'test-directory'
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
					configurations.test.singleFile
				}
			}
		'''

		expect:
		fails('verify')
	}
}
