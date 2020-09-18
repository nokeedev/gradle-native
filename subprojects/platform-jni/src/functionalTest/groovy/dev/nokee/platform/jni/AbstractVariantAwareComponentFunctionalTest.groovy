package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.platform.jni.fixtures.*
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement
import spock.lang.Requires
import spock.util.environment.OperatingSystem

import static org.apache.commons.io.FilenameUtils.separatorsToSystem

abstract class AbstractVariantAwareComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
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
					tasks.register("custom${variant.identifier.fullName.capitalize()}") {
						group = 'Custom'
						description = "Custom task for variant '${variant.identifier.fullName}'."
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
customLinux${currentArchitecture.capitalize()} - Custom task for variant 'linux${currentArchitecture.capitalize()}'.
customMacos${currentArchitecture.capitalize()} - Custom task for variant 'macos${currentArchitecture.capitalize()}'.
customWindows${currentArchitecture.capitalize()} - Custom task for variant 'windows${currentArchitecture.capitalize()}'.
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
					configurations.create("custom${variant.identifier.fullName.capitalize()}") {
						description = "Custom configuration for variant '${variant.identifier.fullName}'."
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
customLinux${currentArchitecture.capitalize()} - Custom configuration for variant 'linux${currentArchitecture.capitalize()}'.
No dependencies

customMacos${currentArchitecture.capitalize()} - Custom configuration for variant 'macos${currentArchitecture.capitalize()}'.
No dependencies

customWindows${currentArchitecture.capitalize()} - Custom configuration for variant 'windows${currentArchitecture.capitalize()}'.
No dependencies
""")
	}

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
					configurations.create("custom${variant.identifier.fullName.capitalize()}Elements") {
						description = "Custom configuration for variant '${variant.identifier.fullName}'."
						canBeConsumed = true
						canBeResolved = false
						outgoing.artifact(file("build/${variant.identifier.fullName}.potato"))
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
Variant customLinux${currentArchitecture.capitalize()}Elements
--------------------------------------------------
Description = Custom configuration for variant 'linux${currentArchitecture.capitalize()}'.

Artifacts
    - build/linux${currentArchitecture.capitalize()}.potato (artifactType = potato)

--------------------------------------------------
Variant customMacos${currentArchitecture.capitalize()}Elements
--------------------------------------------------
Description = Custom configuration for variant 'macos${currentArchitecture.capitalize()}'.

Artifacts
    - build/macos${currentArchitecture.capitalize()}.potato (artifactType = potato)

--------------------------------------------------
Variant customWindows${currentArchitecture.capitalize()}Elements
--------------------------------------------------
Description = Custom configuration for variant 'windows${currentArchitecture.capitalize()}'.

Artifacts
    - build/windows${currentArchitecture.capitalize()}.potato (artifactType = potato)
"""))
	}

	def "can view dependency insight for configuration created by variants when executing dependencyInsight"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << '''
			repositories {
				gradlePluginPortal()
			}

			def configuredVariants = []
			library {
				variants.configureEach { variant ->
					configuredVariants << variant
					configurations.create("custom${variant.identifier.fullName.capitalize()}") {
						description = "Custom configuration for variant '${variant.identifier.fullName}'."
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
		result.assertOutputContains("""
> Task :dependencyInsight
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
		return '1.3.72'
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
}
