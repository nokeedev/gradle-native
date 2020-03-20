package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.platform.jni.fixtures.GreeterAppWithJniLibrary

import java.util.concurrent.TimeUnit

class JavaApplicationWithJavaCppJniLibraryDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec  {
	private void makeComponentWithLibrary() {
		settingsFile << '''
			rootProject.name = 'application'
			include 'jni-library'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}
		'''
		file('jni-library/build.gradle') << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''

		componentsUnderTest.withLibraryAsSubproject('jni-library').writeToProject(testDirectory)
	}

	private void makeComponentWithLibraries() {
		settingsFile << '''
			rootProject.name = 'application'
			include 'jni-library'
			include 'java-library'
			include 'cpp-library'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}
		'''
		file('jni-library/build.gradle') << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			library {
				dependencies {
					jvmImplementation project(':java-library')
					nativeImplementation project(':cpp-library')
				}
			}
		'''
		file('java-library/build.gradle') << '''
			plugins {
				id 'java-library'
			}
		'''
		file('cpp-library/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}

			library {
				linkage = [Linkage.STATIC]
				binaries.configureEach {
					compileTask.get().positionIndependentCode = true
				}
			}
		'''
	}

	private void makeComponentWithIncludedBuildLibrary() {
		settingsFile << '''
			rootProject.name = 'application'
			includeBuild 'jni-library'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}
		'''

		file('jni-library/settings.gradle') << "rootProject.name = 'jni-library'"
		file('jni-library/build.gradle') << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			group = 'com.example'
			version = '4.2'
		'''
	}

	private void makeComponentWithIncludedBuildLibraries() {
		settingsFile << '''
			rootProject.name = 'application'
			includeBuild 'jni-library'
			includeBuild 'java-library'
			includeBuild 'cpp-library'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}
		'''
		file('jni-library/settings.gradle') << "rootProject.name = 'jni-library'"
		file('jni-library/build.gradle') << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			group = 'com.example'
			version = '4.2'

			library {
				dependencies {
					jvmImplementation 'com.example:java-library:4.2'
					nativeImplementation 'com.example:cpp-library:4.2'
				}
			}
		'''
		file('java-library/settings.gradle') << "rootProject.name = 'java-library'"
		file('java-library/build.gradle') << '''
			plugins {
				id 'java-library'
			}

			group = 'com.example'
			version = '4.2'
		'''
		file('cpp-library/settings.gradle') << "rootProject.name = 'cpp-library'"
		file('cpp-library/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}

			group = 'com.example'
			version = '4.2'

			library {
				linkage = [Linkage.STATIC]
				binaries.configureEach {
					compileTask.get().positionIndependentCode = true
				}
			}
		'''
	}

	private GreeterAppWithJniLibrary getComponentsUnderTest() {
		return new GreeterAppWithJniLibrary('application')
	}

	def "can define implementation dependencies on component"() {
		makeComponentWithLibrary()
		buildFile << '''
			dependencies {
				implementation project(':jni-library')
			}
		'''

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can define an included build implementation dependencies on component"() {
		makeComponentWithIncludedBuildLibrary()
		buildFile << '''
			dependencies {
				implementation 'com.example:jni-library:4.2'
			}
		'''
		componentsUnderTest.withResourcePath('com/example/').withLibraryAsSubproject('jni-library').writeToProject(testDirectory)

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can define implementation dependencies on component with target machines"() {
		makeComponentWithLibrary()
		buildFile << '''
			dependencies {
				implementation project(':jni-library')
			}
		'''
		file('jni-library/build.gradle') << '''
			library {
				targetMachines = [machines.windows, machines.linux, machines.macOS]
			}
		'''
		componentsUnderTest.withResourcePath("application/${currentOsFamilyName}/").withLibraryAsSubproject('jni-library').writeToProject(testDirectory)

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can define an included build implementation dependencies on component with target machines"() {
		makeComponentWithIncludedBuildLibrary()
		buildFile << '''
			dependencies {
				implementation 'com.example:jni-library:4.2'
			}
		'''
		file('jni-library/build.gradle') << '''
			library {
				targetMachines = [machines.windows, machines.linux, machines.macOS]
			}
		'''
		componentsUnderTest.withResourcePath("com/example/${currentOsFamilyName}/").withLibraryAsSubproject('jni-library').writeToProject(testDirectory)

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can consume transitive dependencies on component"() {
		given:
		makeComponentWithLibraries()
		buildFile << '''
			dependencies {
				implementation project(':jni-library')
			}
		'''
		[componentsUnderTest.library.jvmBindings.withResourcePath('application/').withSharedLibraryBaseName('jni-library'), componentsUnderTest.library.nativeBindings]*.writeToProject(file('jni-library'))
		componentsUnderTest.library.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.library.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.application.writeToProject(testDirectory)

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can consume an included build transitive dependencies on component"() {
		makeComponentWithIncludedBuildLibraries()
		buildFile << '''
			dependencies {
				implementation 'com.example:jni-library:4.2'
			}
		'''
		[componentsUnderTest.library.jvmBindings.withSharedLibraryBaseName('jni-library').withResourcePath('com/example/'), componentsUnderTest.library.nativeBindings]*.writeToProject(file('jni-library'))
		componentsUnderTest.library.jvmImplementation.writeToProject(file('java-library'))
		componentsUnderTest.library.nativeImplementation.asLib().writeToProject(file('cpp-library'))
		componentsUnderTest.application.writeToProject(testDirectory)

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}

	def "can run distribution on a clean machine"() {
		given:
		makeComponentWithLibrary()
		buildFile << """
			dependencies {
				implementation project(':jni-library')
			}
		"""

		and:
		run('distZip')
		file('build/distributions/application.zip').copyTo(file('application-distribution.zip'))
		run('clean')

		when:
		unzipTo(file('application-distribution.zip'), file('application.zip'))

		then:
		file('application.zip/application/bin/application').exec().out.contains(componentsUnderTest.expectedOutput)
	}

	// TODO: Migrate to TestFile
	void unzipTo(TestFile zipFile, File workingDirectory) {
		zipFile.assertIsFile()
		workingDirectory.mkdirs()
		assertSuccessfulExecution(['unzip', zipFile.getCanonicalPath(), '-d', workingDirectory.getCanonicalPath()])
	}

	private void assertSuccessfulExecution(List<String> commandLine, File workingDirectory = null) {
		def process = commandLine.execute(null, workingDirectory)
		def stdoutThread = Thread.start { process.in.eachByte { print(new String(it)) } }
		def stderrThread = Thread.start { process.err.eachByte { print(new String(it)) } }
		assert process.waitFor(30, TimeUnit.SECONDS)
		assert process.exitValue() == 0
		stdoutThread.join(5000)
		stderrThread.join(5000)
	}
}
