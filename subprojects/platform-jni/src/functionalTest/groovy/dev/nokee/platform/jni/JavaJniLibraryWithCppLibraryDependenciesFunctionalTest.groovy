package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class JavaJniLibraryWithCppLibraryDependenciesFunctionalTest extends AbstractFunctionalSpec {
	def "can depends on C++ static library project"() {
		settingsFile << '''
			rootProject.name = 'jni-greeter'
			include 'cpp-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
                mavenCentral()
            }

			// TODO: can use `library` DSL?
			dependencies {
				nativeImplementation project(':cpp-greeter')
				testImplementation 'junit:junit:4.12'
			}
		'''
		file('cpp-greeter/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}

			library.linkage.set([Linkage.STATIC])
		'''
		def fixture = new  JavaJniCppGreeterLib()
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(file('cpp-greeter'))

		when:
		succeeds('assemble')

		then:
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/Greeter.class')
		jar('build/libs/jni-greeter-macos-x86-64.jar').hasDescendants('libjni-greeter.dylib')
		result.assertTaskNotExecuted(':cpp-greeter:compileReleaseCpp')
		result.assertTaskNotExecuted(':cpp-greeter:createRelease')
	}

	def "can depends on C++ shared library project"() {
		settingsFile << '''
			rootProject.name = 'jni-greeter'
			include 'cpp-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
                mavenCentral()
            }

			// TODO: can use `library` DSL?
			dependencies {
				nativeImplementation project(':cpp-greeter')
				testImplementation 'junit:junit:4.12'
			}
		'''
		file('cpp-greeter/build.gradle') << '''
			plugins {
				id 'cpp-library'
			}

			library.linkage.set([Linkage.SHARED])
		'''
		def fixture = new  JavaJniCppGreeterLib()
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(file('cpp-greeter'))

		when:
		succeeds('assemble')

		then:
		jar('build/libs/jni-greeter.jar').hasDescendants('com/example/greeter/Greeter.class')
		jar('build/libs/jni-greeter-macos-x86-64.jar').hasDescendants('libcpp-greeter.dylib', 'libjni-greeter.dylib')
		result.assertTaskNotExecuted(':cpp-greeter:compileReleaseCpp')
		result.assertTaskNotExecuted(':cpp-greeter:linkRelease')
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}
}
