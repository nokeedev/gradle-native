/*
 * Copyright 2020-2021 the original author or authors.
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
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.language.DefaultNativeProjectTasks
import dev.nokee.language.c.CTaskNames
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.jni.fixtures.*
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement
import spock.lang.Ignore
import spock.lang.Requires
import spock.util.environment.OperatingSystem

abstract class AbstractNativeLinkingAwareComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can link native objects using binary-specific lifecycle tasks"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds(taskNames.binary)

		then:
		result.assertTasksExecuted(additionalCompileTasks, taskNames.allToBinary)
		and:
		sharedLibrary('build/libs/main/jni-greeter').assertExists()
	}

	def "can link native sources of a specific variant using binary-specific lifecycle tasks"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines('machines.windows', 'machines.macOS', 'machines.linux')

		when:
		succeeds(taskNames.withOperatingSystemFamily(currentOsFamilyName).binary)

		then:
		result.assertTasksExecuted(additionalCompileTasks, taskNames.withOperatingSystemFamily(currentOsFamilyName).allToBinary)
		and:
		sharedLibrary("build/libs/main/${currentOsFamilyName}/jni-greeter").assertExists()
	}

	@Ignore('https://github.com/nokeedev/gradle-native/issues/488')
	def "only resolves the targeted variant using binary-specific lifecycle tasks"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines('machines.windows', 'machines.macOS', 'machines.linux')

		and:
		buildFile << configureResolvedVariantsAssertion(1)

		expect:
		succeeds(taskNames.withOperatingSystemFamily(currentOsFamilyName).binary)
	}

	protected abstract void makeSingleProject()

	protected abstract JniLibraryElement getComponentUnderTest()

	protected abstract DefaultNativeProjectTasks getTaskNames()

	protected abstract List<String> getAdditionalCompileTasks()

	protected String getHostVariantName() {
		return "${currentOsFamilyName}${currentArchitecture.capitalize()}"
	}

	protected String getCurrentPlatformName() {
		return "${currentOsFamilyName}${currentArchitecture}"
	}

	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	protected configureTargetMachines(String... targetMachines) {
		return """
			${componentUnderTestDsl} {
				targetMachines = [${targetMachines.join(",")}]
			}
		"""
	}

	protected String configureResolvedVariantsAssertion(int expectedVariantToResolve) {
		return """
			def configuredVariants = []
			${componentUnderTestDsl} {
				variants.configureEach {
					configuredVariants << it
				}
			}
			gradle.buildFinished {
				assert configuredVariants.size() == ${expectedVariantToResolve}
			}
		"""
	}
}

class NativeLinkingJavaCppJniLibraryFunctionalTest extends AbstractNativeLinkingAwareComponentFunctionalTest implements CppTaskNames {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}

	@Override
	protected DefaultNativeProjectTasks getTaskNames() {
		return tasks.forSharedLibrary
	}

	@Override
	protected List<String> getAdditionalCompileTasks() {
		return [':compileJava']
	}
}

class NativeLinkingJavaCJniLibraryFunctionalTest extends AbstractNativeLinkingAwareComponentFunctionalTest implements CTaskNames {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.c-language'
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}

	@Override
	protected DefaultNativeProjectTasks getTaskNames() {
		return tasks.forSharedLibrary
	}

	@Override
	protected List<String> getAdditionalCompileTasks() {
		return [':compileJava']
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class NativeLinkingJavaObjectiveCJniLibraryFunctionalTest extends AbstractNativeLinkingAwareComponentFunctionalTest implements ObjectiveCTaskNames {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter')
	}

	@Override
	protected DefaultNativeProjectTasks getTaskNames() {
		return tasks.forSharedLibrary
	}

	@Override
	protected List<String> getAdditionalCompileTasks() {
		return [':compileJava']
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class NativeLinkingJavaObjectiveCppJniLibraryFunctionalTest extends AbstractNativeLinkingAwareComponentFunctionalTest implements ObjectiveCppTaskNames {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter')
	}

	@Override
	protected DefaultNativeProjectTasks getTaskNames() {
		return tasks.forSharedLibrary
	}

	@Override
	protected List<String> getAdditionalCompileTasks() {
		return [':compileJava']
	}
}

class NativeLinkingKotlinCppJniLibraryFunctionalTest extends AbstractNativeLinkingAwareComponentFunctionalTest implements CppTaskNames {
	private static String getKotlinVersion() {
		return '1.6.21'
	}

	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'org.jetbrains.kotlin.jvm'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
				mavenCentral()
			}

			dependencies {
				implementation platform('org.jetbrains.kotlin:kotlin-bom:${kotlinVersion}')
				implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'
			}
		"""
		settingsFile << """
			pluginManagement {
				repositories {
					gradlePluginPortal()
					mavenCentral()
				}

				resolutionStrategy {
					eachPlugin {
						if (requested.id.id == 'org.jetbrains.kotlin.jvm') {
							useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
						}
					}
				}
			}
		"""
		settingsFile << '''
			gradle.rootProject {
				repositories {
					mavenCentral()
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new KotlinJniCppGreeterLib('jni-greeter')
	}

	@Override
	protected DefaultNativeProjectTasks getTaskNames() {
		return tasks.forSharedLibrary
	}

	@Override
	protected List<String> getAdditionalCompileTasks() {
		return [':compileJava', ':compileKotlin']
	}
}
