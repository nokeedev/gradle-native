package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JavaGreeter
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem

class JniLibraryFunctionalTest extends AbstractFunctionalSpec {
	def "can consume transitive JVM API dependencies from no language JNI library"() {
		settingsFile << '''
			include 'consumer', 'jni-library', 'producer'
		'''
		file('consumer/build.gradle') << '''
			plugins {
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}

			dependencies {
				implementation project(':jni-library')
			}
		'''
		file('jni-library/build.gradle') << '''
			plugins {
				id 'dev.nokee.jni-library'
			}

			dependencies {
				api project(':producer')
			}
		'''
		file('producer/build.gradle') << '''
			plugins {
				id 'java-library'
			}
		'''
		new JavaMainUsesGreeter().writeToProject(file('consumer'))
		new JavaGreeter().writeToProject(file('producer'))

		when:
		succeeds(':consumer:run')

		then:
		outputContains('Bonjour, World!')
	}

	def "can consume transitive native runtime dependencies from no language JNI library"() {
		settingsFile << '''
			include 'consumer', 'jni-library', 'producer'
		'''
		file('consumer/build.gradle') << '''
			plugins {
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}

			dependencies {
				implementation project(':jni-library')
			}
		'''
		file('jni-library/build.gradle') << '''
			plugins {
				id 'dev.nokee.jni-library'
			}

			dependencies {
				nativeImplementation project(':producer')
			}
		'''
		file('producer/build.gradle') << """
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
		new JavaMainUsesGreeter().writeToProject(file('consumer'))
		def fixture = new JavaJniCppGreeterLib('producer')
		fixture.jvmImplementation.writeToProject(file('consumer'))
		fixture.nativeImplementation.writeToProject(file('producer'))
		fixture.jniBindings.withJniGeneratedHeader().writeToProject(file('producer'))

		when:
		succeeds(':consumer:run')

		then:
		outputContains('Bonjour, World!')
	}

	def "can consume no language JNI project"() {
		settingsFile << '''
			include 'consumer', 'jni-library'
		'''
		file('consumer/build.gradle') << '''
			plugins {
				id 'application'
			}

			application {
				mainClassName = 'com.example.app.Main'
			}

			dependencies {
				implementation project(':jni-library')
			}
		'''
		file('jni-library/build.gradle') << '''
			plugins {
				id 'dev.nokee.jni-library'
			}
		'''
		new JavaMainUsesGreeter().writeToProject(file('consumer'))
		new JavaGreeter().writeToProject(file('consumer'))

		when:
		succeeds(':consumer:run')

		then:
		outputContains('Bonjour, World!')
	}
}
