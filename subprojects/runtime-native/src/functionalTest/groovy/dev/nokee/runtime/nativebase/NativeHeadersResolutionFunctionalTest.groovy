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
import static org.apache.commons.io.FilenameUtils.separatorsToUnix

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
				test files('${separatorsToUnix(compress(createTestHeaders(testDirectory)).absolutePath)}')
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
				test files('${separatorsToUnix(createTestHeaders(testDirectory).absolutePath)}')
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
				outgoing.artifact(file('${separatorsToUnix(compress(createTestHeaders(testDirectory)).absolutePath)}')) {
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
				outgoing.artifact(file('${separatorsToUnix(createTestHeaders(file('lib')).absolutePath)}')) {
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
				from('${separatorsToUnix(headersLocation.absolutePath)}')
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
					artifact(file('${separatorsToUnix(headersLocation.absolutePath)}')) {
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
