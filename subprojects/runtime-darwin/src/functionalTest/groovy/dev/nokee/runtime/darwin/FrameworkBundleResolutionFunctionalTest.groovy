package dev.nokee.runtime.darwin

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.CGreeter
import spock.lang.Unroll

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static dev.nokee.runtime.darwin.OutgoingElements.outgoingFrameworkElements
import static dev.nokee.runtime.darwin.OutgoingElements.outgoingHeadersElements
import static dev.nokee.runtime.darwin.VerifyTask.verifyTask
import static dev.nokee.runtime.darwin.internal.DarwinArtifactTypes.LINKABLE_ELEMENT_OR_FRAMEWORK_TYPE
import static dev.nokee.runtime.darwin.internal.DarwinArtifactTypes.NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE

class FrameworkBundleResolutionFunctionalTest extends AbstractGradleSpecification {
	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.darwin-runtime'
			}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.C_PLUS_PLUS_API))
//					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.HEADERS_CPLUSPLUS))
//					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
			}
		"""
		executer = executer.withArgument('-i')
	}

	private static TestFile createTestFramework(TestFile destinationDirectory) {
		def frameworkLocation = destinationDirectory.createDirectory('Test.framework')
		def version = frameworkLocation.file('Versions/A')
		def headers = version.file('Headers')

		new CGreeter().publicHeaders.writeToSourceDir(headers)
		frameworkLocation.file('Versions/Current').createSymbolicLink(version)
		frameworkLocation.file('Headers').createSymbolicLink(headers)
		return frameworkLocation
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

	def "can resolve adhoc compressed framework bundle from disk"() {
		def frameworkLocation = compress(createTestFramework(testDirectory))
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).transformed('Test.framework') }
		buildFile << """
			configurations.test {
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
				}
			}

			dependencies {
				test files('${frameworkLocation}')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.assertOutputContains('Transforming Test.framework.zip with UnzipTransform')
		result.assertOutputContains('Transforming Test.framework.zip with FrameworkToCompilerReady')
	}

	def "can resolve adhoc framework bundle from disk"() {
		def frameworkLocation = createTestFramework(testDirectory)
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('Test.framework')) }
		buildFile << """
			configurations.test {
				attributes {
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
				}
			}

			dependencies {
				test files('${frameworkLocation}')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.assertOutputContains('Transforming Test.framework with DirectoryToFramework')
		result.assertOutputContains('Transforming Test.framework with FrameworkToCompilerReady')
	}

	def "can resolve compressed framework bundle from subproject"() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingFrameworkElements().mainVariant { it.artifact(compress(createTestFramework(file('lib')))) }
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).transformed('Test.framework') }
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		succeeds('verify')
	}

	@Unroll
	def "can resolve framework bundle from subproject [#directoryType]"(String directoryType) {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingFrameworkElements()
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type(directoryType) }
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('lib/Test.framework')) }
		if (directoryType == 'directory') {
			buildFile << '''
				configurations.test {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
					}
				}
			'''
		}
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		def result = succeeds('verify')
		if (directoryType == 'directory') {
			result.assertOutputContains('Transform Test.framework (project :lib) with DirectoryToFramework')
		}
		result.assertOutputContains('Transform Test.framework (project :lib) with FrameworkToCompilerReady')

		where:
		directoryType << ['directory', 'framework']
	}

	def "resolve uncompressed framework bundle from subproject when dual artifact variant exists"() {
		settingsFile << '''
			include 'lib'
		'''
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements()
			.mainVariant { it.artifact(frameworkLocation) }
			.variant('zip') { it.artifact(compress(frameworkLocation)) }
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('lib/Test.framework')) }
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		def result = succeeds('verify')
		result.assertOutputContains('Transform Test.framework (project :lib) with FrameworkToCompilerReady')
	}


	private static TestFile createTestHeaders(TestFile destinationDirectory) {
		def fixture = new CGreeter()
		def includeDirectory = destinationDirectory.createDirectory('includes')
		fixture.publicHeaders.writeToSourceDir(includeDirectory)
		return includeDirectory
	}


	@Unroll
	def "prefers header search paths to framework bundle [header: #headerType, framework: #frameworkType]"(headerType, frameworkType) {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingHeadersElements('testHeadersElements')
			.mainVariant {it.artifact(createTestHeaders(file('lib'))).type(headerType) }
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type(frameworkType) }
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('lib/includes')) }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		if (headerType == 'directory') {
			result.assertOutputContains('Transform includes (project :lib) with DirectoryToHeaderSearchPath')
		}
		result.assertOutputContains('Transform includes (project :lib) with HeaderSearchPathToCompilerReady')

		where:
		[headerType, frameworkType] << collectEachCombination([['directory', 'native-headers-directory'], ['directory', 'framework']])
	}

	@Unroll
	def "can select framework bundle instead of header search paths [header: #headerType, framework: #frameworkType]"(headerType, frameworkType) {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingHeadersElements('testHeadersElements')
			.mainVariant { it.artifact(createTestHeaders(file('lib'))).type(headerType) }
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type(frameworkType) }
		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('lib/Test.framework')) }
		buildFile << """
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
					}
				}
			}
		"""

		expect:
		def result = succeeds('verify')
		if (frameworkType == 'directory') {
			result.assertOutputContains('Transform Test.framework (project :lib) with DirectoryToFramework')
		}
		result.assertOutputContains('Transform Test.framework (project :lib) with FrameworkToCompilerReady')

		where:
		[headerType, frameworkType] << collectEachCombination([['directory', 'native-headers-directory'], ['directory', 'framework']])
	}

	private static List<List<String>> collectEachCombination(List<List> values) {
		def result = []
		values.eachCombination {
			result << it
		}
		return result
	}


	def "can resolve framework bundle from subproject when dual artifact variant exists"() {
		settingsFile << '''
			include 'lib'
		'''
		def includesLocation = createTestHeaders(file('lib'))
		file('lib/build.gradle') << outgoingHeadersElements('testHeadersElements')
			.mainVariant { it.artifact(includesLocation) }
			.variant('zip') { it.artifact(compress(includesLocation)) }
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(frameworkLocation) }
			.variant('zip') { it.artifact(compress(frameworkLocation)) }

		buildFile << verifyTask().that { it.artifactType(NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE).directory(file('lib/Test.framework')) }
		buildFile << """
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, "framework-bundle"))
					}
				}
			}
		"""

		expect:
		def result = succeeds('verify')
		result.assertOutputContains('Transform Test.framework (project :lib) with FrameworkToCompilerReady')
	}

	def "can resolve framework for native-link usage from subproject when dual artifact variant exists"() {
		settingsFile << '''
			include 'lib'
		'''
		def includesLocation = createTestHeaders(file('lib'))
		file('lib/build.gradle') << outgoingHeadersElements('testHeadersElements')
			.mainVariant { it.artifact(includesLocation) }
			.variant('zip') { it.artifact(compress(includesLocation)) }
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(frameworkLocation) }
			.variant('zip') { it.artifact(compress(frameworkLocation)) }
		buildFile << verifyTask().that { it.artifactType(LINKABLE_ELEMENT_OR_FRAMEWORK_TYPE).directory(file('lib/Test.framework')) }
		buildFile << """
			configurations.test {
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, Usage.NATIVE_LINK))
				}
			}
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		result.assertOutputContains('Transform Test.framework (project :lib) with FrameworkToLinkerReady')
	}

	// TODO: dual import-lib and framework
	// TODO: dual dynlib and framework
}
