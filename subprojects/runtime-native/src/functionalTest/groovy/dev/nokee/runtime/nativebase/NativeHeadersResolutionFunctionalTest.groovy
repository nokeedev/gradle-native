package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.CGreeter
import spock.lang.Unroll

class NativeHeadersResolutionFunctionalTest extends AbstractGradleSpecification {
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

			def verifyTask = tasks.register('verify') {
				ext.resolvedFiles = configurations.test.incoming.artifactView {
					attributes {
						attribute(Attribute.of('artifactType', String), 'native-headers-directory')
					}
				}.files
				dependsOn(resolvedFiles)

				doLast {
					println resolvedFiles.singleFile
					assert resolvedFiles.singleFile.directory
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
		def headersLocation = createTestHeaders(testDirectory)
		buildFile << verifyTransformed('headers') << """
			def zipTask = tasks.register('zipHeaders', Zip) {
				from('${headersLocation}')
				destinationDirectory = buildDir
				archiveBaseName = 'headers'
			}

			dependencies {
				test files(zipTask)
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can resolve adhoc header search directories"() {
		def headersLocation = createTestHeaders(testDirectory)
		buildFile << verifyDirectory(headersLocation) << """
			dependencies {
				test files('${headersLocation}')
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can resolve compressed headers from project"() {
		settingsFile << '''
			include 'lib'
		'''
		def headersLocation = createTestHeaders(testDirectory)
		file('lib/build.gradle') << """
			def zipTask = tasks.register('zipTestHeaders', Zip) {
				from('${headersLocation}')
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
				outgoing.artifact(zipTask) {
					type = 'zip'
				}
			}
		"""
		buildFile << verifyTransformed('headers') << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')
	}

	@Unroll
	def "can resolve header search directories from project [#directoryType]"(String directoryType) {
		settingsFile << '''
			include 'lib'
		'''
		def headersLocation = createTestHeaders(file('lib'))
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${headersLocation}')) {
					type = '${directoryType}'
				}
			}
		"""
		buildFile << verifyDirectory(file('lib/includes')) << """
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
				from('${headersLocation}')
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
					artifact(file('${headersLocation}')) {
						type = 'native-headers-directory'
					}
					variants.create('zip') {
						artifact(zipTask) {
							type = 'zip'
						}
					}
				}
			}
		"""
		buildFile << verifyDirectory(file('lib/includes')) << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')
	}

	private static String verifyTransformed(String fileName) {
		return """
			verifyTask.configure {
				doLast {
					assert resolvedFiles.singleFile.name == '${fileName}'
					assert resolvedFiles.singleFile.parentFile.name == 'transformed' ||
						resolvedFiles.singleFile.path.contains('/.transforms/') ||
						resolvedFiles.singleFile.path.contains('/transforms-')
				}
			}
		"""
	}

	private static String verifyDirectory(File path) {
		return """
			verifyTask.configure {
				doLast {
					assert resolvedFiles.singleFile.absolutePath == '${path}'
					assert resolvedFiles.singleFile.parentFile.name != 'transformed' &&
						!resolvedFiles.singleFile.path.contains('/.transforms/') &&
						!resolvedFiles.singleFile.path.contains('/transforms-')
				}
			}
		"""
	}
}
