/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.jni

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.platform.nativebase.SharedLibraryBinary
import org.junit.Assume

abstract class AbstractJniLibraryFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "produce a single JAR containing the shared library for single variant"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		file('build/libs').assertHasDescendants(*expectSharedLibrary('main/jni-greeter'), 'jni-greeter.jar')
		jar("build/libs/jni-greeter.jar").hasDescendants(*expectedClasses, sharedLibraryName('jni-greeter'))
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}

	protected abstract void makeSingleProject()

	abstract SourceElement getComponentUnderTest()

	// TODO: Move to native fixtures
	protected List<String> expectSharedLibrary(Object path) {
		List<String> result = new ArrayList<String>()

		result.add(sharedLibraryName(path))
		if (toolChain.isVisualCpp()) {
			result.add(importLibraryName(path))
			result.add(exportFileName(path))
		}
		return result
	}

	private String importLibraryName(Object path) {
		return path.toString() + '.lib'
	}

	private String exportFileName(Object path) {
		return path.toString() + '.exp'
	}

	protected String configureProjectGroup(String groupId) {
		return """
			group = '${groupId}'
		"""
	}

	private static collectEachPermutation(values) {
		def result = []
		values.eachPermutation {
			result << it
		}
		return result
	}

	protected static String configurePlugins(List<String> pluginIds) {
		return """
			plugins {
				${pluginIds.collect { "id '$it'"}.join('\n')}
			}
		"""
	}

	def "can apply plugins in what ever order"(pluginIds) {
		given:
		settingsFile << "rootProject.name = 'library'"
		buildFile << configurePlugins(pluginIds)

		when:
		succeeds('assemble')

		then:
		jar('build/libs/library.jar')

		where:
		pluginIds << collectEachPermutation([jvmPluginId, 'dev.nokee.jni-library', nativePluginId])
	}

	protected abstract String getJvmPluginId()

	protected abstract String getNativePluginId()

	protected abstract List<String> getExpectedClasses()

	def "build logic can change build directory location"() {
		Assume.assumeFalse("the Kotlin plugin seems to not behave properly", getClass().getSimpleName().contains("Kotlin"));
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        given:
        buildFile << '''
            buildDir = 'output'
         '''

        expect:
        succeeds 'assemble'

        !file('build').exists()
		file('output/libs').assertHasDescendants(*expectSharedLibrary('main/jni-greeter'), 'jni-greeter.jar')
		jar("output/libs/jni-greeter.jar").hasDescendants(*expectedClasses, sharedLibraryName('jni-greeter'))
    }

	def "generates a empty JAR when no souce"() {
        given:
        makeSingleProject()

        expect:
        succeeds 'assemble'
		jar('build/libs/jni-greeter.jar').hasDescendants()
    }

	def "uses the project group as the default resource path for the shared library"() {
		makeSingleProject()
		buildFile << configureProjectGroup('com.example.greeter')
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants(*expectedClasses, sharedLibraryName('com/example/greeter/jni-greeter'))
	}

	def "can configure the resource path for each variant"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			library {
				variants.configureEach {
					resourcePath = 'com/example/foobar'
				}
			}
		"""

		when:
		succeeds('assemble')

		then:
		jar("build/libs/jni-greeter.jar").hasDescendants(*expectedClasses, sharedLibraryName('com/example/foobar/jni-greeter'))
	}

	def "can query the binaries for single-variant library"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			import ${JarBinary.canonicalName}
			import ${JniJarBinary.canonicalName}
			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}

			tasks.register('verify') {
				doLast {
					def variants = library.variants.elements
					assert variants.get().size() == 1

					def binaries = variants.get().first().binaries.elements
					assert binaries.get().size() == 2
					assert binaries.get().count { it instanceof ${JarBinary.simpleName} } == 1
					assert binaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 1
					assert binaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 0
					assert binaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 1

					def allBinaries = library.binaries.elements
					assert allBinaries.get().size() == 3
					assert allBinaries.get().count { it instanceof ${JarBinary.simpleName} } == 2
					assert allBinaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 1
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	def "resolve variants when all the binaries are queried"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)
		buildFile << """
			import ${JarBinary.canonicalName}
			import ${JniJarBinary.canonicalName}
			import ${JvmJarBinary.canonicalName}
			import ${SharedLibraryBinary.canonicalName}

			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
				}
			}

			tasks.register('verify') {
				doLast {
					def allBinaries = library.binaries.elements
					assert allBinaries.get().size() == 3
					assert allBinaries.get().count { it instanceof ${JarBinary.simpleName} } == 2
					assert allBinaries.get().count { it instanceof ${JniJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${JvmJarBinary.simpleName} } == 1
					assert allBinaries.get().count { it instanceof ${SharedLibraryBinary.simpleName} } == 1

					assert configuredVariants.size() == 1
				}
			}
		"""

		expect:
		succeeds('verify')
	}
}
