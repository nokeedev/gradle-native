package dev.nokee.platform.jni


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.platform.jni.fixtures.GreeterAppWithJniLibrary
import dev.nokee.platform.jni.fixtures.elements.JavaGreeter
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem

class JavaApplicationWithNoLanguageJniLibraryDependenciesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
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

		when:
		run('run')

		then:
		result.assertOutputContains(componentsUnderTest.expectedOutput)
	}


	private GreeterAppWithJniLibrary getComponentsUnderTest() {
		return new GreeterAppWithJniLibrary('cpp-library', 'application/')
	}

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
				id 'dev.nokee.jni-library'
			}
		'''

		new JavaMainUsesGreeter().writeToProject(testDirectory)
		new JavaGreeter().writeToProject(testDirectory)
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
		file('jni-library/build.gradle') << '''
			plugins {
				id 'dev.nokee.jni-library'
			}

			group = 'com.example'
			version = '4.2'
		'''

		new JavaMainUsesGreeter().writeToProject(testDirectory)
		new JavaGreeter().writeToProject(testDirectory)
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
				id 'dev.nokee.jni-library'
			}

			library {
				dependencies {
					api project(':java-library')
					nativeImplementation project(':cpp-library')
				}
			}
		'''
		file('java-library/build.gradle') << '''
			plugins {
				id 'java-library'
			}
		'''
		file('cpp-library/build.gradle') << """
			plugins {
				id 'cpp-library'
			}

			import ${Jvm.canonicalName}
			import ${OperatingSystem.canonicalName}

			library {
				binaries.configureEach {
					compileTask.get().getIncludes().from(project.provider {
						def result = [new File("\${Jvm.current().javaHome.absolutePath}/include")]

						if (OperatingSystem.current().isMacOsX()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/darwin"))
						} else if (OperatingSystem.current().isLinux()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/linux"))
						} else if (OperatingSystem.current().isWindows()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/win32"))
						}
						return result
					})
				}
			}
		"""

		[componentsUnderTest.library.nativeBindings.withJniGeneratedHeader(), componentsUnderTest.library.nativeImplementation]*.writeToProject(file('cpp-library'))
		[componentsUnderTest.library.jvmBindings, componentsUnderTest.library.jvmImplementation]*.writeToProject(file('java-library'))
		componentsUnderTest.application.writeToProject(testDirectory)
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
				id 'dev.nokee.jni-library'
			}

			group = 'com.example'
			version = '4.2'

			library {
				dependencies {
					api 'com.example:java-library:4.2'
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
		file('cpp-library/build.gradle') << """
			plugins {
				id 'cpp-library'
			}

			group = 'com.example'
			version = '4.2'

			import ${Jvm.canonicalName}
			import ${OperatingSystem.canonicalName}

			library {
				binaries.configureEach {
					compileTask.get().getIncludes().from(project.provider {
						def result = [new File("\${Jvm.current().javaHome.absolutePath}/include")]

						if (OperatingSystem.current().isMacOsX()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/darwin"))
						} else if (OperatingSystem.current().isLinux()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/linux"))
						} else if (OperatingSystem.current().isWindows()) {
							result.add(new File("\${Jvm.current().javaHome.absolutePath}/include/win32"))
						}
						return result
					})
				}
			}
		"""

		[componentsUnderTest.library.nativeBindings.withJniGeneratedHeader(), componentsUnderTest.library.nativeImplementation]*.writeToProject(file('cpp-library'))
		[componentsUnderTest.library.jvmBindings.withResourcePath('com/example/'), componentsUnderTest.library.jvmImplementation]*.writeToProject(file('java-library'))
		componentsUnderTest.application.writeToProject(testDirectory)
	}
}
