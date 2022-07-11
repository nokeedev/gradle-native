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
import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.language.c.CSourceSet
import dev.nokee.language.cpp.CppSourceSet
import dev.nokee.language.nativebase.NativeHeaderSet
import dev.nokee.language.objectivec.ObjectiveCSourceSet
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet
import dev.nokee.language.swift.SwiftSourceSet

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
		result.assertTasksExecuted(tasks.allToLifecycleAssemble, tasks(':library').allToLink)
	}

	def "can generate sources"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToSourceDir(file('srcs'))
		buildFile << """
			import ${LanguageSourceSet.canonicalName}

			def generatedSources = tasks.register('generateSources') {
				def inputFiles = fileTree('srcs')
				def outputDir = layout.buildDirectory.dir('generated-srcs-for-native')

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

			${componentUnderTestDsl}.sources.configureEach({ sourceSet -> ['Java', 'Groovy', 'Kotlin'].every { !sourceSet.getClass().simpleName.contains(it) } }) {
				setFrom(generatedSources)
			}

			import ${CSourceSet.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${ObjectiveCSourceSet.canonicalName}
			import ${ObjectiveCppSourceSet.canonicalName}
			import ${SwiftSourceSet.canonicalName}
			${componentUnderTestDsl}.sources.configureEach(CSourceSet) { filter.include('**/*.c') }
			${componentUnderTestDsl}.sources.configureEach(CppSourceSet) { filter.include('**/*.cpp') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCSourceSet) { filter.include('**/*.m') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCppSourceSet) { filter.include('**/*.mm') }
			${componentUnderTestDsl}.sources.configureEach(SwiftSourceSet) { filter.include('**/*.swift') }
		"""

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
			import ${LanguageSourceSet.canonicalName}
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
				def outputDir = layout.buildDirectory.dir('generated-srcs-for-native')

				inputs.files(inputFiles)
				outputs.dir(outputDir)
				doLast {
					def out = outputDir.get()

					inputFiles.each {
						out.file(it.name).asFile.text = it.text
					}
				}
			}

			library.sources.configureEach({ sourceSet -> ['Java', 'Groovy', 'Kotlin'].every { !sourceSet.getClass().simpleName.contains(it) } }) {
				from(generatedSources)
			}
			import ${CSourceSet.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${ObjectiveCSourceSet.canonicalName}
			import ${ObjectiveCppSourceSet.canonicalName}
			import ${SwiftSourceSet.canonicalName}
			library.sources.configureEach(CSourceSet) { filter.include('**/*.c') }
			library.sources.configureEach(CppSourceSet) { filter.include('**/*.cpp') }
			library.sources.configureEach(ObjectiveCSourceSet) { filter.include('**/*.m') }
			library.sources.configureEach(ObjectiveCppSourceSet) { filter.include('**/*.mm') }
			library.sources.configureEach(SwiftSourceSet) { filter.include('**/*.swift') }
		"""

		expect:
		succeeds(':assemble')
		result.assertTasksExecuted(tasks.allToAssemble, tasks(':library').allToLink, ':library:generateSources')
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

	protected boolean isLegacy() {
		return true
	}

	protected String configureSourcesAsConvention(String dsl = componentUnderTestDsl, boolean legacy = isLegacy()) {
		return """
			import ${LanguageSourceSet.canonicalName}
			pluginManager.withPlugin('java') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir('srcs')
			}
			afterEvaluate {
				pluginManager.withPlugin('java') {
					${dsl}.sources.configureEach(CSourceSet) { headers.from(${dsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${dsl}.sources.configureEach(CppSourceSet) { headers.from(${dsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${dsl}.sources.configureEach(ObjectiveCSourceSet) { headers.from(${dsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${dsl}.sources.configureEach(ObjectiveCppSourceSet) { headers.from(${dsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
				}
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

			import ${NativeHeaderSet.canonicalName}
			import ${CSourceSet.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${ObjectiveCSourceSet.canonicalName}
			import ${ObjectiveCppSourceSet.canonicalName}
			import ${SwiftSourceSet.canonicalName}
			import ${LanguageSourceSet.canonicalName}

			${dsl} {
				sources.configureEach(NativeHeaderSet) {
					if (it.name == 'public') {
						setFrom('includes')
					} else {
						setFrom('headers')
					}
				}
				sources.configureEach({ sourceSet -> !(sourceSet instanceof NativeHeaderSet) && ['Java', 'Groovy', 'Kotlin'].every { !sourceSet.getClass().simpleName.contains(it) } }) {
					setFrom('srcs')
				}
			}
			${dsl}.sources.configureEach(CSourceSet) { filter.include('**/*.c') }
			${dsl}.sources.configureEach(CppSourceSet) { filter.include('**/*.cpp') }
			${dsl}.sources.configureEach(ObjectiveCSourceSet) { filter.include('**/*.m') }
			${dsl}.sources.configureEach(ObjectiveCppSourceSet) { filter.include('**/*.mm') }
			${dsl}.sources.configureEach(SwiftSourceSet) { filter.include('**/*.swift') }
		""" + (legacy ? '' : """
			${dsl}.sources.configureEach(CSourceSet) { headers.setFrom('headers', 'includes') }
			${dsl}.sources.configureEach(CppSourceSet) { headers.setFrom('headers', 'includes') }
			${dsl}.sources.configureEach(ObjectiveCSourceSet) { headers.setFrom('headers', 'includes') }
			${dsl}.sources.configureEach(ObjectiveCppSourceSet) { headers.setFrom('headers', 'includes') }
			${dsl}.sources.configureEach(SwiftSourceSet) { headers.setFrom('headers', 'includes') }
		""")
	}

	protected String configureSourcesAsExplicitFiles() {
		return """
			import ${LanguageSourceSet.canonicalName}
			pluginManager.withPlugin('java-base') {
				sourceSets.main.java.setSrcDirs([])
				sourceSets.main.java.filter.include('**/*.java')
				sourceSets.main.java.srcDir('srcs')
			}
			afterEvaluate {
				pluginManager.withPlugin('java-base') {
					${componentUnderTestDsl}.sources.configureEach(CSourceSet) { headers.from(${componentUnderTestDsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${componentUnderTestDsl}.sources.configureEach(CppSourceSet) { headers.from(${componentUnderTestDsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${componentUnderTestDsl}.sources.configureEach(ObjectiveCSourceSet) { headers.from(${componentUnderTestDsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
					${componentUnderTestDsl}.sources.configureEach(ObjectiveCppSourceSet) { headers.from(${componentUnderTestDsl}.sources.java.flatMap { it.compileTask }.flatMap { it.options.headerOutputDirectory }) }
				}
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

			import ${NativeHeaderSet.canonicalName}

			${componentUnderTestDsl} {
				sources.configureEach(NativeHeaderSet) {
					if (it.name == 'public') {
						setFrom('includes')
					} else {
						setFrom('headers')
					}
				}
				sources.configureEach({ sourceSet -> !(sourceSet instanceof NativeHeaderSet) && ['Java', 'Groovy', 'Kotlin'].every { !sourceSet.getClass().simpleName.contains(it) } }) {
					setFrom(${ofSources(componentUnderTest).files.collect { "'srcs/${it.name}'" }.join(',')})
				}
			}

			import ${CSourceSet.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${ObjectiveCSourceSet.canonicalName}
			import ${ObjectiveCppSourceSet.canonicalName}
			import ${SwiftSourceSet.canonicalName}
			${componentUnderTestDsl}.sources.configureEach(CSourceSet) { filter.include('**/*.c') }
			${componentUnderTestDsl}.sources.configureEach(CppSourceSet) { filter.include('**/*.cpp') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCSourceSet) { filter.include('**/*.m') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCppSourceSet) { filter.include('**/*.mm') }
			${componentUnderTestDsl}.sources.configureEach(SwiftSourceSet) { filter.include('**/*.swift') }
		""" + (legacy ? '' : """
			${componentUnderTestDsl}.sources.configureEach(CSourceSet) { headers.setFrom('headers', 'includes') }
			${componentUnderTestDsl}.sources.configureEach(CppSourceSet) { headers.setFrom('headers', 'includes') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCSourceSet) { headers.setFrom('headers', 'includes') }
			${componentUnderTestDsl}.sources.configureEach(ObjectiveCppSourceSet) { headers.setFrom('headers', 'includes') }
			${componentUnderTestDsl}.sources.configureEach(SwiftSourceSet) { headers.setFrom('headers', 'includes') }
		""")
	}
}
