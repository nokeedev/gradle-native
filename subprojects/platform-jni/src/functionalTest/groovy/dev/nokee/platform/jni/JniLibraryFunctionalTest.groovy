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


import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JavaGreeter
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter
import org.gradle.internal.jvm.Jvm
import org.gradle.internal.os.OperatingSystem

class JniLibraryFunctionalTest extends AbstractInstalledToolChainIntegrationSpec implements JavaApplicationFixture {
	def "can consume transitive JVM API dependencies from no language JNI library"() {
		settingsFile << '''
			include 'consumer', 'jni-library', 'producer'
		'''
		file('consumer/build.gradle') << '''
			plugins {
				id 'application'
			}

			dependencies {
				implementation project(':jni-library')
			}
		''' << configureJavaApplicationMainClassName('com.example.app.Main')
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
		succeeds ':consumer:run'

		then:
		outputContains('Bonjour, World!')
	}

	def "can consume transitive native runtime dependencies from no language JNI library"() {
		settingsFile << '''
			rootProject.name = 'library'
			include 'consumer', 'jni-library', 'producer'
		'''
		file('consumer/build.gradle') << '''
			plugins {
				id 'application'
			}

			dependencies {
				implementation project(':jni-library')
			}
		''' << configureJavaApplicationMainClassName('com.example.app.Main')
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
		fixture.jvmBindings.withResourcePath('library/').writeToProject(file('consumer'))
		fixture.jvmImplementation.writeToProject(file('consumer'))
		fixture.nativeImplementation.writeToProject(file('producer'))
		fixture.nativeBindings.withJniGeneratedHeader().writeToProject(file('producer'))

		when:
		succeeds ':consumer:run'

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

			dependencies {
				implementation project(':jni-library')
			}
		''' << configureJavaApplicationMainClassName('com.example.app.Main')
		file('jni-library/build.gradle') << '''
			plugins {
				id 'dev.nokee.jni-library'
			}
		'''
		new JavaMainUsesGreeter().writeToProject(file('consumer'))
		new JavaGreeter().writeToProject(file('consumer'))

		when:
		succeeds ':consumer:run'

		then:
		outputContains('Bonjour, World!')
	}

	def "produces an empty JNI Jar"() {
		settingsFile << "rootProject.name = 'library'"
		buildFile << '''
			plugins {
				id 'dev.nokee.jni-library'
			}
		'''

		when:
		succeeds 'assemble'

		then:
		jar('build/libs/library.jar').hasDescendants()
	}

	def "ignores all language implementations"() {
		buildFile << '''
			plugins {
				id 'dev.nokee.jni-library'
			}
		'''
		file('src/main/java/broken.java') << 'broken!'
		file('src/main/cpp/broken.cpp') << 'broken!'
		file('src/main/c/broken.c') << 'broken!'

		expect:
		succeeds 'assemble'
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}
}
