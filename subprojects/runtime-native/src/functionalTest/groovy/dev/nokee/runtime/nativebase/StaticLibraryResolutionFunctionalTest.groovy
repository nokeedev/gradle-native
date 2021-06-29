package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.api.attributes.Usage
import spock.lang.Unroll

import static dev.nokee.runtime.nativebase.VerifyTask.verifyTask

class StaticLibraryResolutionFunctionalTest extends AbstractGradleSpecification {
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
	def "can resolve adhoc static library file only for link usage [#staticLibraryExtension]"(String staticLibraryExtension) {
		def staticLib = file("libtest.${staticLibraryExtension}").createFile()

		// NOTE: We have to verify artifact by type for adhoc files
		buildFile << verifyTask()
			.that { "testLink.${it.artifactType(staticLibraryExtension)}.singleFile.path == '${file("libtest.${staticLibraryExtension}")}'" }
			.that { "testRuntime.${it.artifactType(staticLibraryExtension)}.empty" }
		buildFile << """
			dependencies {
				test files('${staticLib}')
			}
		"""

		expect:
		succeeds('verify')

		where:
		staticLibraryExtension << ['lib', 'a']
	}

	@Unroll
	def "can resolve static library from remote project only for link usage [#staticLibraryExtension]"(String staticLibraryExtension) {
		def staticLib = file("lib/test.${staticLibraryExtension}").createFile()
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			configurations.create('testElements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, '${Usage.NATIVE_LINK}'))
					attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.LINK_ARCHIVE))
					attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
				}
				outgoing.artifact(file('${staticLib}')) {
					type = '${staticLibraryExtension}'
				}
			}
		"""
		buildFile << verifyTask()
			.that { "testLink.${it.allFiles()}.singleFile.path == '${file("lib/test.${staticLibraryExtension}")}'" }
			.that { "testRuntime.${it.allFiles()}.empty" }
		buildFile << """
			dependencies {
				test project(':lib')
			}
		"""

		expect:
		succeeds('verify')

		where:
		staticLibraryExtension << ['lib', 'a']
	}
}
