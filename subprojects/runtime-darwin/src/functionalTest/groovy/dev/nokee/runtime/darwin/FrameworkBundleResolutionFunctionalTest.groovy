package dev.nokee.runtime.darwin

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.runtime.base.ArtifactTransformFixture
import spock.lang.Unroll

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static dev.nokee.runtime.base.VerifyTask.uncompressedFiles
import static dev.nokee.runtime.darwin.OutgoingElements.outgoingFrameworkElements
import static dev.nokee.runtime.darwin.OutgoingElements.outgoingHeadersElements
import static dev.nokee.runtime.base.VerifyTask.verifyTask

class FrameworkBundleResolutionFunctionalTest extends AbstractGradleSpecification implements ArtifactTransformFixture {
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
		buildFile << verifyTask()
			.that { "configurations.test.${uncompressedFiles()}.singleFile.name == 'Test.framework'" }
			.that { "transformed(configurations.test.${uncompressedFiles()})" }
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
		result.output =~ /Transforming( artifact)? Test.framework.zip with UnzipTransform/
	}

	def "can resolve adhoc framework bundle from disk"() {
		def frameworkLocation = createTestFramework(testDirectory)
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('Test.framework')" }
			.that { "!transformed(configurations.test)" }
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
		doesNotTransformArtifacts(result.output)
	}

	def "can resolve compressed framework bundle from subproject"() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingFrameworkElements().mainVariant { it.artifact(compress(createTestFramework(file('lib')))).type('framework-zip') }
		buildFile << verifyTask()
			.that { "configurations.test.${uncompressedFiles()}.singleFile.name == 'Test.framework'" }
			.that { "transformed(configurations.test.${uncompressedFiles()})" }
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		def result = succeeds('verify')
		result.output =~ /Transform(ing artifact)? Test.framework.zip \(project :lib\) with FrameworkArchiveToFramework/
	}

	def "can resolve framework bundle from subproject"() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << outgoingFrameworkElements()
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type('framework') }
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/Test.framework')" }
			.that { "!transformed(configurations.test)" }
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}

	def "resolve uncompressed framework bundle from subproject when dual artifact variant exists"() {
		settingsFile << '''
			include 'lib'
		'''
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements()
			.mainVariant { it.artifact(frameworkLocation) }
			.variant('zip') { it.artifact(compress(frameworkLocation)).type('framework-zip') }
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/Test.framework')" }
			.that { "!transformed(configurations.test)" }
		buildFile << '''
			dependencies {
				test project(':lib')
			}
		'''

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
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
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type('framework') }
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/includes')" }
			.that { "!transformed(configurations.test)" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		if (headerType == 'directory') {
			result.output =~ /Transform(ing artifact)? includes \(project :lib\) with DirectoryToHeaderSearchPath/
		}

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
			.mainVariant { it.artifact(createTestFramework(file('lib'))).type('framework') }
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/Test.framework')" }
			.that { "!transformed(configurations.test)" }
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
		doesNotTransformArtifacts(result.output)

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
			.mainVariant { it.artifact(includesLocation).type('native-headers-directory') }
			.variant('zip') { it.artifact(compress(includesLocation)).type('native-headers-zip') }
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(frameworkLocation).type('framework') }
			.variant('zip') { it.artifact(compress(frameworkLocation)).type('framework-zip') }

		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/Test.framework')" }
			.that { "!transformed(configurations.test)" }
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
		doesNotTransformArtifacts(result.output)
	}

	def "can resolve framework for native-link usage from subproject when dual artifact variant exists"() {
		settingsFile << '''
			include 'lib'
		'''
		def includesLocation = createTestHeaders(file('lib'))
		file('lib/build.gradle') << outgoingHeadersElements('testHeadersElements')
			.mainVariant { it.artifact(includesLocation).type('native-headers-directory') }
			.variant('zip') { it.artifact(compress(includesLocation)).type('native-headers-zip') }
		def frameworkLocation = createTestFramework(file('lib'))
		file('lib/build.gradle') << outgoingFrameworkElements('testFrameworkElements')
			.mainVariant { it.artifact(frameworkLocation).type('framework') }
			.variant('zip') { it.artifact(compress(frameworkLocation)).type('framework-zip') }
		buildFile << verifyTask()
			.that { "configurations.test.singleFile == file('lib/Test.framework')" }
			.that { "!transformed(configurations.test)" }
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
		doesNotTransformArtifacts(result.output)
	}

	// TODO: dual import-lib and framework
	// TODO: dual dynlib and framework
}
