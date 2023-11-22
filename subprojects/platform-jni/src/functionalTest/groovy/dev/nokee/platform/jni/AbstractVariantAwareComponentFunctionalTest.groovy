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
import dev.nokee.platform.jni.fixtures.*
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement
import spock.lang.Ignore
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Unroll
import spock.util.environment.OperatingSystem

import static dev.nokee.utils.VersionNumber.parse
import static org.apache.commons.io.FilenameUtils.separatorsToSystem

abstract class AbstractVariantAwareComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	@Unroll
	@Ignore('https://github.com/nokeedev/gradle-native/issues/488')
	def "do not realize variants for unrelated tasks"(task) {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			def configuredVariants = []
			library {
				variants.configureEach {
					configuredVariants << it
				}
			}

            gradle.buildFinished {
                assert configuredVariants == []
            }

            tasks.register('foo')
		"""

		expect:
		succeeds(task)

		where:
		task << ['help', 'projects', 'foo']
	}

	def "can see tasks created by variants when executing tasks"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << '''
			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
					project.tasks.register("custom${variant.name.capitalize()}") {
						group = 'Custom'
						description = "Custom task for variant '${variant.name}'."
					}
				}
			}

            gradle.buildFinished {
                assert configuredVariants.size() == 3
            }
		'''

		expect:
		succeeds('tasks')
		result.assertOutputContains("""Custom tasks
------------
customLinux - Custom task for variant 'linux'.
customMacos - Custom task for variant 'macos'.
customWindows - Custom task for variant 'windows'.
""")
	}

	def "can see configurations created by variants when executing dependencies"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << '''
			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
					configurations.create("custom${variant.name.capitalize()}") {
						description = "Custom configuration for variant '${variant.name}'."
					}
				}
			}

            gradle.buildFinished {
                assert configuredVariants.size() == 3
            }
		'''

		expect:
		succeeds('dependencies')
		result.assertOutputContains("""
customLinux - Custom configuration for variant 'linux'.
No dependencies

customMacos - Custom configuration for variant 'macos'.
No dependencies

customWindows - Custom configuration for variant 'windows'.
No dependencies
""")
	}

	@IgnoreIf({ parse(System.getProperty('dev.gradleplugins.defaultGradleVersion')) >= parse("7.5") })
	def "can see outgoing variants created by variants when executing outgoingVariants"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << '''
			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
					configurations.create("custom${variant.name.capitalize()}Elements") {
						description = "Custom configuration for variant '${variant.name}'."
						canBeConsumed = true
						canBeResolved = false
						outgoing.artifact(file("build/${variant.name}.potato"))
					}
				}
			}

            gradle.buildFinished {
                assert configuredVariants.size() == 3
            }
		'''

		expect:
		succeeds('outgoingVariants')
		result.assertOutputContains(separatorsToSystem("""
--------------------------------------------------
Variant customLinuxElements
--------------------------------------------------
Description = Custom configuration for variant 'linux'.

Artifacts
    - build/linux.potato (artifactType = potato)

--------------------------------------------------
Variant customMacosElements
--------------------------------------------------
Description = Custom configuration for variant 'macos'.

Artifacts
    - build/macos.potato (artifactType = potato)

--------------------------------------------------
Variant customWindowsElements
--------------------------------------------------
Description = Custom configuration for variant 'windows'.

Artifacts
    - build/windows.potato (artifactType = potato)
"""))
	}

	@IgnoreIf({ parse(System.getProperty('dev.gradleplugins.defaultGradleVersion')) >= parse("7.5") })
	def "can view dependency insight for configuration created by variants when executing dependencyInsight"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << '''
			repositories {
				mavenCentral()
			}

			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
					configurations.create("custom${variant.name.capitalize()}") {
						description = "Custom configuration for variant '${variant.name}'."
						canBeConsumed = false
						canBeResolved = true
						dependencies.add(project.dependencies.create('dev.nokee:platformJni:0.3.0'))
					}
				}
			}

            gradle.buildFinished {
                assert configuredVariants.size() == 3
            }
		'''

		expect:
		succeeds('dependencyInsight', '--configuration', "custom${hostVariantName.capitalize()}", '--dependency', 'dev.nokee:platformJni:0.3.0')
		result.assertOutputContains("""> Task :dependencyInsight
dev.nokee:platformJni:0.3.0
   variant "runtimeElements" [
      org.gradle.category            = library (not requested)
      org.gradle.dependency.bundling = external (not requested)
      org.gradle.jvm.version         = 8 (not requested)
      org.gradle.libraryelements     = jar (not requested)
      org.gradle.usage               = java-runtime (not requested)
      org.gradle.status              = release (not requested)
   ]

dev.nokee:platformJni:0.3.0
\\--- custom${hostVariantName.capitalize()}""")
	}

	protected abstract void makeSingleProject()

	protected abstract JniLibraryElement getComponentUnderTest()

	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	protected String getHostVariantName() {
		return "${currentOsFamilyName}${currentArchitecture.capitalize()}"
	}

	protected configureTargetMachines(String... targetMachines) {
		return """
            ${componentUnderTestDsl} {
                targetMachines = [${targetMachines.join(",")}]
            }
        """
	}
}

class VariantAwareComponentJavaCJniLibraryFunctionalTest extends AbstractVariantAwareComponentFunctionalTest {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.c-language'
			}
		'''
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}
}

class VariantAwareComponentJavaCppJniLibraryFunctionalTest extends AbstractVariantAwareComponentFunctionalTest {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class VariantAwareComponentJavaObjectiveCJniLibraryFunctionalTest extends AbstractVariantAwareComponentFunctionalTest {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}
		'''
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib('jni-greeter')
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class VariantAwareComponentJavaObjectiveCppJniLibraryFunctionalTest extends AbstractVariantAwareComponentFunctionalTest {
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}
		'''
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected JniLibraryElement getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter')
	}
}

class VariantAwareComponentKotlinCppJniLibraryFunctionalTest extends AbstractVariantAwareComponentFunctionalTest {
	private static String getKotlinVersion() {
		def gradleVersionUnderTest = parse(System.getProperty("dev.gradleplugins.defaultGradleVersion"));
		if (parse("6.8.3") <= gradleVersionUnderTest) {
			return "1.8.10"
		} else if (parse("6.7.1").compareTo(gradleVersionUnderTest) <= 0) {
			return "1.7.20"
		} else {
			return '1.6.21'
		}
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
		buildFile << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << """
			pluginManagement {
				repositories {
					gradlePluginPortal()
					mavenCentral()
				}

				resolutionStrategy {
					eachPlugin {
						if (requested.id.id == 'org.jetbrains.kotlin.jvm') {
							useVersion("${kotlinVersion}")
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
}
