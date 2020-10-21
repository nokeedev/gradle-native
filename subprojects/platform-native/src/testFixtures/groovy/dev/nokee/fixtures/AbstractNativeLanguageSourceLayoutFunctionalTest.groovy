package dev.nokee.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.language.c.CHeaderSet
import dev.nokee.language.cpp.CppHeaderSet

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
		file('library', buildFileName) << configureSourcesAsConvention('library')

		expect:
		succeeds ':assemble'
		result.assertTasksExecuted(tasks.allToLifecycleAssemble, tasks(':library').allToLink)
	}

	def "can generate sources"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToSourceDir(file('srcs'))
		buildFile << """
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
				sourceSets.main.java {
					setSrcDirs([generatedSources])
					filter.include('**/*.java')
				}
			}

			${componentUnderTestDsl}.sources.configureEach {
				from(generatedSources)
			}
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
				sourceSets.main.java {
					setSrcDirs([generatedSources])
					filter.include('**/*.java')
				}
			}

			library.sources.configureEach {
				from(generatedSources)
			}
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
//
//		if (getClass().simpleName.contains('Swift')) {
//			component.writeToSourceDir(projectDir.file('srcs'))
//		} else if (getClass().simpleName.contains('Jni')) {
//			component.jvmSources.writeToProject(projectDir)
//			component.nativeSources.sources.writeToSourceDir(projectDir.file('srcs'))
//			component.nativeSources.headers.writeToSourceDir(projectDir.file('headers'))
//		} else {
//			component.sources.writeToSourceDir(projectDir.file('srcs'))
//
//			if (getClass().simpleName.contains('Library') || !component.publicHeaders.empty) {
//				component.privateHeaders.writeToSourceDir(projectDir.file('headers'))
//				component.publicHeaders.writeToSourceDir(projectDir.file('includes'))
//			} else {
//				component.headers.writeToSourceDir(projectDir.file('headers'))
//			}
//		}
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

	protected String configureSourcesAsConvention(String dsl = componentUnderTestDsl) {
		return """
			pluginManager.withPlugin('java') {
				sourceSets.main.java {
					setSrcDirs(['srcs'])
					filter.include('**/*.java')
				}
			}

			import ${CHeaderSet.canonicalName}
			import ${CppHeaderSet.canonicalName}

			${dsl} {
				sources.configureEach({ it instanceof ${CHeaderSet.simpleName} || it instanceof ${CppHeaderSet.simpleName} }) {
					if (it.identifier.name.get() == 'public') {
						from('includes')
					} else {
						from('headers')
					}
				}
				sources.configureEach({ !(it instanceof ${CHeaderSet.simpleName} || it instanceof ${CppHeaderSet.simpleName}) }) {
					from('srcs')
				}
			}
		"""
	}

	protected String configureSourcesAsExplicitFiles() {
		return """
			// We don't test Gradle Java plugin
			pluginManager.withPlugin('java') {
				sourceSets.main.java {
					setSrcDirs(['srcs'])
					filter.include('**/*.java')
				}
			}

			import ${CHeaderSet.canonicalName}
			import ${CppHeaderSet.canonicalName}

			${componentUnderTestDsl} {
				sources.configureEach({ it instanceof ${CHeaderSet.simpleName} || it instanceof ${CppHeaderSet.simpleName} }) {
					if (it.identifier.name.get() == 'public') {
						from('includes')
					} else {
						from('headers')
					}
				}
				sources.configureEach({ !(it instanceof ${CHeaderSet.simpleName} || it instanceof ${CppHeaderSet.simpleName}) }) {
					${ofSources(componentUnderTest).files.collect { "from('srcs/${it.name}')" }.join('\n')}
				}
			}
		"""
	}
}
