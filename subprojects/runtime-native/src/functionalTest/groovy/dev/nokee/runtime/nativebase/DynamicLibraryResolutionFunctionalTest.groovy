package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.api.attributes.Usage
import spock.lang.Unroll

import static dev.nokee.runtime.nativebase.VerifyTask.verifyTask
import static org.apache.commons.io.FilenameUtils.separatorsToUnix

class DynamicLibraryResolutionFunctionalTest extends AbstractGradleSpecification {
	protected String filePath(Object... path) {
		return separatorsToUnix(file(path).path)
	}

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
	def "can resolve adhoc nix shared library file for link and runtime [#sharedLibraryExtension]"(String sharedLibraryExtension) {
		def sharedLib = file("libtest.${sharedLibraryExtension}").createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "testLink.${it.artifactType(sharedLibraryExtension)}.singleFile == file('${filePath("libtest.${ sharedLibraryExtension}")}')" }
			.that { "testRuntime.${it.artifactType(sharedLibraryExtension)}.singleFile == file('${filePath("libtest.${sharedLibraryExtension}")}')" }
		buildFile << """
			dependencies {
				test files('${sharedLib}')
			}
		"""

		expect:
		succeeds('verify')

		where:
		sharedLibraryExtension << ['so', 'dylib']
	}

	def "can resolve adhoc windows shared library file only for runtime usage"() {
		def sharedLib = file('test.dll').createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "testLink.${it.artifactType('dll')}.empty" }
			.that { "testRuntime.${it.artifactType('dll')}.singleFile == file('${filePath('test.dll')}')" }
		buildFile << """
			dependencies {
				test files('${sharedLib}')
			}
		"""

		expect:
		succeeds('verify')
	}

	@Unroll
	def "can resolve nix shared library from remote project for link and runtime usage [#sharedLibraryExtension]"(String sharedLibraryExtension) {
		def sharedLib = file("lib/libtest.${sharedLibraryExtension}").createFile()
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_LINK}+${Usage.NATIVE_RUNTIME}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.DYNAMIC_LIB))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${sharedLib}')) {
					type = '${sharedLibraryExtension}'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "testLink.${it.allFiles()}.singleFile == file('${filePath("lib/libtest.${sharedLibraryExtension}")}')" }
			.that { "testRuntime.${it.allFiles()}.singleFile == file('${filePath("lib/libtest.${sharedLibraryExtension}")}')" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')

		where:
		sharedLibraryExtension << ['so', 'dylib']
	}

	def "can resolve windows shared library from remote project only for runtime usage"() {
		def sharedLib = file('lib/test.dll').createFile()
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_RUNTIME}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.DYNAMIC_LIB))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${sharedLib}')) {
					type = 'dll'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "testLink.${it.allFiles()}.empty" }
			.that { "testRuntime.${it.allFiles()}.singleFile == file('${filePath('lib/test.dll')}')" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can resolve adhoc windows shared library with import library files"() {
		def sharedLib = file('test.dll').createFile()
		def importLib = file('test.lib').createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "testLink.${it.artifactType('lib')}.singleFile == file('${filePath('test.lib')}')" }
			.that { "testRuntime.${it.artifactType('dll')}.singleFile == file('${filePath('test.dll')}')" }
			.that { "testRuntime.${it.artifactType('lib')}.empty" } // import lib doesn't leak in runtime
		buildFile << """
			dependencies {
				test files('${sharedLib}', '${importLib}')
			}
		"""

		expect:
		succeeds('verify')
	}

	def "can resolve windows shared library with import library from remote project"() {
		def sharedLib = file('lib/test.dll').createFile()
		def importLib = file('lib/test.lib').createFile()
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testLinkElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_LINK}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, 'import-lib'))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${importLib}')) {
					type = 'lib'
				}
			}

			configurations.create('testRuntimeElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_RUNTIME}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.DYNAMIC_LIB))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${sharedLib}')) {
					type = 'dll'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "testLink.incoming.files.singleFile == file('${filePath('lib/test.lib')}')" } // only file resolved
			.that { "testRuntime.incoming.files.singleFile == file('${filePath('lib/test.dll')}')" } // only file resolved
			.that { "testRuntime.${it.artifactType('lib')}.empty" } // import lib doesn't leak in runtime
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')
	}
}
