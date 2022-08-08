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
package dev.nokee.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.file.TestFile

import static dev.gradleplugins.fixtures.sources.NativeLibraryElement.ofPrivateHeaders
import static dev.gradleplugins.fixtures.sources.NativeLibraryElement.ofPublicHeaders
import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofSources

abstract class AbstractNativeLanguageSourceLayoutFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {

	def "can change source layout convention"() {
		given:
		makeSingleProject()
		writeToProjectWithCustomLayout(componentUnderTest)
		buildFile << configureSourcesAsConvention()

		expect:
		succeeds ":assemble"
		result.assertTasksExecuted(tasks.allToLifecycleAssemble)

		// TODO: Improve this assertion
		file("build/objs/main").assertIsDirectory()
	}

	def "can add individual source files"() {
		given:
		makeSingleProject()
		writeToProjectWithCustomLayout(componentUnderTest)
		buildFile << configureSourcesAsExplicitFiles()

		expect:
		succeeds ":assemble"
		result.assertTasksExecuted(tasks.allToLifecycleAssemble)

		// TODO: Improve this assertion
		file("build/objs/main").assertIsDirectory()
	}

	def "can depends on library with custom source layout"() {
		given:
		makeProjectWithLibrary()

		and:
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
		writeToProjectWithCustomLayout(fixture.elementUsingGreeter)
		writeToProjectWithCustomLayout(fixture.greeter, file('library'))

		and:
		buildFile << configureSourcesAsConvention()
		file('library', buildFileName) << configureSourcesAsConvention('library', true)

		expect:
		succeeds ':assemble'
		result.assertTasksExecuted(tasks.allToLifecycleAssemble, tasks(':library').allToLinkElements)
	}

	def "can generate sources"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToSourceDir(file('srcs'))
		buildFile << """
			def generatedSources = tasks.register('generateSources') {
				def inputFiles = fileTree('srcs')
				ext.outputDir = layout.buildDirectory.dir('generated-srcs-for-native')

				inputs.files(inputFiles)
				outputs.dir(outputDir)
				doLast {
					def out = outputDir.get()

					inputFiles.each {
						out.file(it.name).asFile.text = it.text
					}
				}
			}

			pluginManager.withPlugin('java') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir(generatedSources)
			}
			pluginManager.withPlugin('groovy') {
				sourceSets.main.groovy.setSrcDirs([])
				sourceSets.main.groovy.filter.include('**/*.groovy')
				sourceSets.main.groovy.srcDir(generatedSources)
			}
			pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
				sourceSets.main.kotlin.setSrcDirs([])
				sourceSets.main.kotlin.filter.include('**/*.kt')
				sourceSets.main.kotlin.srcDir(generatedSources)
			}

			pluginManager.withPlugin('dev.nokee.c-language-base') {
				${componentUnderTestDsl}.cSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.c') })
				${componentUnderTestDsl}.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.cpp-language-base') {
				${componentUnderTestDsl}.cppSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.cpp') })
				${componentUnderTestDsl}.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.objective-c-language-base') {
				${componentUnderTestDsl}.objectiveCSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.m') })
				${componentUnderTestDsl}.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.objective-cpp-language-base') {
				${componentUnderTestDsl}.objectiveCppSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.mm') })
				${componentUnderTestDsl}.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.swift-language-base') {
				${componentUnderTestDsl}.swiftSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.swift') })
			}
		"""
		if (this.class.simpleName.contains('Library') && !this.class.simpleName.contains('Swift') && !this.class.simpleName.contains('Jni')) {
			buildFile << """
				${componentUnderTestDsl}.publicHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			"""
		}

		and:
		writeBrokenSourcesAtConventionalLayout()

		expect:
		succeeds('assemble')
		result.assertTasksExecuted(tasks.allToAssemble, ':generateSources')
	}

	def "can depend on library with generated sources"() {
		given:
		makeProjectWithLibrary()

		and:
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
		fixture.elementUsingGreeter.writeToProject(testDirectory)
		fixture.greeter.writeToSourceDir(file('library', 'srcs'))

		and:
		file('library', buildFileName) << """
			pluginManager.withPlugin('java') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir('srcs')
			}
			pluginManager.withPlugin('groovy') {
				sourceSets.main.groovy.setSrcDirs([])
				sourceSets.main.groovy.filter.include('**/*.groovy')
				sourceSets.main.groovy.srcDir('srcs')
			}
			pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
				sourceSets.main.kotlin.setSrcDirs([])
				sourceSets.main.kotlin.filter.include('**/*.kt')
				sourceSets.main.kotlin.srcDir('srcs')
			}

			def generatedSources = tasks.register('generateSources') {
				def inputFiles = fileTree('srcs')
				ext.outputDir = layout.buildDirectory.dir('generated-srcs-for-native')

				inputs.files(inputFiles)
				outputs.dir(outputDir)
				doLast {
					def out = outputDir.get()

					inputFiles.each {
						out.file(it.name).asFile.text = it.text
					}
				}
			}

			pluginManager.withPlugin('dev.nokee.c-language-base') {
				library.cSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.c') })
				library.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
				library.publicHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.cpp-language-base') {
				library.cppSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.cpp') })
				library.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
				library.publicHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.objective-c-language-base') {
				library.objectiveCSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.m') })
				library.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
				library.publicHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.objective-cpp-language-base') {
				library.objectiveCppSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.mm') })
				library.privateHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
				library.publicHeaders.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.h') })
			}

			pluginManager.withPlugin('dev.nokee.swift-language-base') {
				library.swiftSources.setFrom(files(generatedSources.map { it.outputDir.get() }).asFileTree.matching { include('**/*.swift') })
			}
		"""

		expect:
		succeeds(':assemble')
		result.assertTasksExecuted(tasks.allToAssemble, tasks(':library').allToLinkElements, ':library:generateSources')
	}

	protected String getComponentUnderTestDsl() {
		if (getClass().simpleName.contains('Library')) {
			return 'library'
		}
		return 'application'
	}

	protected void writeToProjectWithCustomLayout(SourceElement component, TestFile projectDir = testDirectory) {
		componentUnderTest.files.each {
			projectDir.file("src/main/${it.path}/${it.name}") << "broken!"
		}
		ofSources(component).writeToSourceDir(projectDir.file('srcs'))
		ofPublicHeaders(component).writeToSourceDir(projectDir.file('includes'))
		ofPrivateHeaders(component).writeToSourceDir(projectDir.file('headers'))
	}

	protected void writeBrokenSourcesAtConventionalLayout() {
		file("src/main/c/broken.c") << "broken!"
		file("src/main/cpp/broken.cpp") << "broken!"
		file("src/main/objectiveC/broken.m") << "broken!"
		file("src/main/objc/broken.m") << "broken!"
		file("src/main/objectiveCpp/broken.mm") << "broken!"
		file("src/main/objcpp/broken.mm") << "broken!"
		file("src/main/swift/broken.swift") << "broken!"
	}

	protected abstract SourceElement getComponentUnderTest()

	protected abstract void makeSingleProject()

	protected abstract void makeProjectWithLibrary()

	protected String configureSourcesAsConvention(String dsl = componentUnderTestDsl, boolean isLibrary = this.class.simpleName.contains('Library') && !this.class.simpleName.contains("Jni")) {
		return """
			pluginManager.withPlugin('java') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir('srcs')
			}

			pluginManager.withPlugin('groovy') {
				sourceSets.main.groovy.setSrcDirs([])
				sourceSets.main.groovy.filter.include('**/*.groovy')
				sourceSets.main.groovy.srcDir('srcs')
			}
			pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
				sourceSets.main.kotlin.setSrcDirs([])
				sourceSets.main.kotlin.filter.include('**/*.kt')
				sourceSets.main.kotlin.srcDir('srcs')
			}

			pluginManager.withPlugin('dev.nokee.c-language-base') {
				${dsl}.cSources.setFrom(fileTree('srcs') { include('**/*.c') })
				${dsl}.privateHeaders.setFrom(fileTree('headers') { include('**/*.h') })
				${isLibrary ? "${dsl}.publicHeaders.setFrom(fileTree('includes') { include('**/*.h') })" : ''}
			}

			pluginManager.withPlugin('dev.nokee.cpp-language-base') {
				${dsl}.cppSources.setFrom(fileTree('srcs') { include('**/*.cpp') })
				${dsl}.privateHeaders.setFrom(fileTree('headers') { include('**/*.h') })
				${isLibrary ? "${dsl}.publicHeaders.setFrom(fileTree('includes') { include('**/*.h') })" : ''}
			}

			pluginManager.withPlugin('dev.nokee.objective-c-language-base') {
				${dsl}.objectiveCSources.setFrom(fileTree('srcs') { include('**/*.m') })
				${dsl}.privateHeaders.setFrom(fileTree('headers') { include('**/*.h') })
				${isLibrary ? "${dsl}.publicHeaders.setFrom(fileTree('includes') { include('**/*.h') })" : ''}
			}

			pluginManager.withPlugin('dev.nokee.objective-cpp-language-base') {
				${dsl}.objectiveCppSources.setFrom(fileTree('srcs') { include('**/*.mm') })
				${dsl}.privateHeaders.setFrom(fileTree('headers') { include('**/*.h') })
				${isLibrary ? "${dsl}.publicHeaders.setFrom(fileTree('includes') { include('**/*.h') })" : ''}
			}

			pluginManager.withPlugin('dev.nokee.swift-language-base') {
				${dsl}.swiftSources.setFrom(fileTree('srcs') { include('**/*.swift') })
			}
		"""
	}

	protected String configureSourcesAsExplicitFiles(String dsl = componentUnderTestDsl) {
		def className = this.class.simpleName

		def languageName = ''
		if (className.contains('ObjectiveCpp')) {
			languageName = 'objectiveCpp'
		} else if (className.contains('ObjectiveC')) {
			languageName = 'objectiveC'
		} else if (className.contains('Cpp')) {
			languageName = 'cpp'
		} else if (className.contains('Swift')) {
			languageName = 'swift'
		} else if (className.contains('C')) {
			languageName = 'c'
		}

		def hasPublicHeaders = className.contains('Library') && !className.contains('Jni') && languageName != 'swift'
		def hasPrivateHeaders = languageName != 'swift'

		def result = """
			pluginManager.withPlugin('java-base') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir('srcs')
			}
			pluginManager.withPlugin('groovy-base') {
				sourceSets.main.groovy.setSrcDirs([])
				sourceSets.main.groovy.filter.include('**/*.groovy')
				sourceSets.main.groovy.srcDir('srcs')
			}
			pluginManager.withPlugin('org.jetbrains.kotlin.jvm') {
				sourceSets.main.kotlin.setSrcDirs([])
				sourceSets.main.kotlin.filter.include('**/*.kt')
				sourceSets.main.kotlin.srcDir('srcs')
			}

			${dsl}.${languageName}Sources.setFrom(files(${ofSources(componentUnderTest).files.collect { "'srcs/${it.name}'" }.join(',')}).asFileTree.matching { exclude('**/*.java', '**/*.groovy', '**/*.kt') })
		"""

		if (hasPrivateHeaders) {
			result += """
				${dsl}.privateHeaders.setFrom(fileTree('headers') { include('**/*.h') })
			"""
		}

		if (hasPublicHeaders) {
			result += """
				${dsl}.publicHeaders.setFrom(fileTree('includes') { include('**/*.h') })
			"""
		}

		return result
	}
}
