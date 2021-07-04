package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.runtime.base.ArtifactTransformFixture
import org.gradle.api.attributes.Usage
import spock.lang.Unroll

import static dev.nokee.runtime.base.VerifyTask.allFiles
import static dev.nokee.runtime.base.VerifyTask.artifactType
import static dev.nokee.runtime.base.VerifyTask.verifyTask

class DynamicLibraryResolutionFunctionalTest extends AbstractGradleSpecification implements ArtifactTransformFixture {
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
	def "can resolve adhoc nix shared library file for link and runtime [#ext]"(String ext) {
		def sharedLib = file("libtest.${ext}").createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "configurations.testLink.${artifactType(ext)}.singleFile == file('${file("libtest.${ext}")}')" }
			.that { "configurations.testRuntime.${artifactType(ext)}.singleFile == file('${file("libtest.${ext}")}')" }
		buildFile << """
			dependencies {
				test files('${sharedLib.name}')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)

		where:
		ext << ['so', 'dylib']
	}

	def "can resolve adhoc windows shared library file only for runtime usage"() {
		def sharedLib = file('test.dll').createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "configurations.testLink.${artifactType('dll')}.empty" }
			.that { "configurations.testRuntime.${artifactType('dll')}.singleFile == file('${file('test.dll')}')" }
		buildFile << """
			dependencies {
				test files('${sharedLib.name}')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}

	@Unroll
	def "can resolve nix shared library from remote project for link and runtime usage [#ext]"(String ext) {
		def sharedLib = file("lib/libtest.${ext}").createFile()
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
				outgoing.artifact(file('${sharedLib.name}')) {
					type = '${ext}'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.testLink.${allFiles()}.singleFile == file('${file("lib/libtest.${ext}")}')" }
			.that { "configurations.testRuntime.${allFiles()}.singleFile == file('${file("lib/libtest.${ext}")}')" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)

		where:
		ext << ['so', 'dylib']
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
				outgoing.artifact(file('${sharedLib.name}')) {
					type = 'dll'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.testLink.${allFiles()}.empty" }
			.that { "configurations.testRuntime.${allFiles()}.singleFile == file('${file('lib/test.dll')}')" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}

	def "can resolve adhoc windows shared library with import library files"() {
		def sharedLib = file('test.dll').createFile()
		def importLib = file('test.lib').createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "configurations.testLink.${artifactType('lib')}.singleFile == file('${file('test.lib')}')" }
			.that { "configurations.testRuntime.${artifactType('dll')}.singleFile == file('${file('test.dll')}')" }
			.that { "configurations.testRuntime.${artifactType('lib')}.empty" } // import lib doesn't leak in runtime
		buildFile << """
			dependencies {
				test files('${sharedLib.name}', '${importLib.name}')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
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
				outgoing.artifact(file('${importLib.name}')) {
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
				outgoing.artifact(file('${sharedLib.name}')) {
					type = 'dll'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "configurations.testLink.incoming.files.singleFile == file('${file('lib/test.lib')}')" } // only file resolved
			.that { "configurations.testRuntime.incoming.files.singleFile == file('${file('lib/test.dll')}')" } // only file resolved
			.that { "configurations.testRuntime.${artifactType('lib')}.empty" } // import lib doesn't leak in runtime
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		def result = succeeds('verify')
		doesNotTransformArtifacts(result.output)
	}
}
