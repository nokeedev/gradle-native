package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class VariantAwareNativeComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
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
					tasks.register("custom${variant.name.capitalize()}") {
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
		result.assertOutputContains("""
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
""")
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

	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		''' << configureTargetMachines('machines.macOS', 'machines.windows', 'machines.linux')
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}

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
