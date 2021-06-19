package dev.nokee.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.runtime.nativebase.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.gradle.nativeplatform.toolchain.internal.plugins.StandardToolChainsPlugin
import org.junit.Assume
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsString

// See https://github.com/gradle/gradle-native/issues/982 for the distinction between unknown, unsupported and unbuildable.
abstract class AbstractTargetMachinesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def setup() {
		settingsFile << "rootProject.name = '${projectName}'"
	}

	def "can build on current operating system family and architecture when explicitly specified"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}")

		expect:
		succeeds taskNameToAssembleDevelopmentBinary
		assertComponentUnderTestWasBuilt()
	}

	@Unroll
	def "#taskName task warns when current operating system family is excluded"(taskName) {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureToolChainSupport('some-other-family', currentArchitecture)
		buildFile << configureTargetMachines("machines.os('some-other-family')")

		expect:
		succeeds taskName

		and:
		outputContains("'${componentName}' component in project ':' cannot build on this machine.")

		where:
		taskName << [taskNameToAssembleDevelopmentBinary, 'build']
	}

	@Unroll
	def "#taskName shows warning when current operating system family is excluded only once"(taskName) {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureToolChainSupport('some-other-family', currentArchitecture)
		buildFile << configureTargetMachines("machines.os('some-other-family')")

		expect:
		succeeds taskNameToAssembleDevelopmentBinary

		and:
		result.output.count("'${componentName}' component in project ':' cannot build on this machine.") == 1

		where:
		taskName << [taskNameToAssembleDevelopmentBinary, 'jar']
	}

	def "fails when target machine is unknown by the configured tool chains"() {
		Assume.assumeFalse(this.class.simpleName.contains('Swift')) // TODO: Fix toolchains discovery so we can align Swift with everyone else.
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.os('some-unknown-family')")

		expect:
		fails taskNameToAssembleDevelopmentBinary

		and:
		failure.assertThatCause(containsString("The following target machines are not know by the defined tool chains"))
	}

	def "does not fail when only unsupported target machines is configured but assemble is not invoked"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureToolChainSupport('some-other-family', currentArchitecture)
		buildFile << configureTargetMachines("machines.os('some-other-family')")

		expect:
		succeeds "help"
	}

	def "fails configuration when no target machine is configured"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines()

		expect:
		fails taskNameToAssembleDevelopmentBinary
		failure.assertHasDescription("A problem occurred configuring root project '${projectName}'.")
		failure.assertHasCause("A target machine needs to be specified for component 'main'.")
	}

	def "can build for current machine when multiple target machines are specified"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.linux", "machines.macOS", "machines.windows", "machines.freeBSD")

		expect:
		succeeds taskNameToAssembleDevelopmentBinary
		assertComponentUnderTestWasBuilt(currentOsFamilyName)
	}

	@RequiresInstalledToolChain(ToolChainRequirement.SUPPORTS_32_AND_64)
	def "can build for multiple target machines"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}.x86", "machines.${currentHostOperatingSystemFamilyDsl}.x86_64")

		expect:
		succeeds getTaskNameToAssembleDevelopmentBinaryWithArchitecture(MachineArchitecture.X86), getTaskNameToAssembleDevelopmentBinaryWithArchitecture(MachineArchitecture.X86_64)
		assertComponentUnderTestWasBuilt(MachineArchitecture.X86)
		assertComponentUnderTestWasBuilt(MachineArchitecture.X86_64)
	}

	def "fails when no target architecture can be built"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}.architecture('foo')")
		buildFile << configureToolChainSupport(DefaultNativePlatform.currentOperatingSystem.toFamilyName(), 'foo')

		expect:
		fails taskNameToAssembleDevelopmentBinary
		failure.assertHasCause("No tool chain is available to build for platform '${currentOsFamilyName}foo'")
		// The error message should probably be more precise like `No tool chain is available to build C++`
	}

	def "can build current architecture when other, non-buildable architectures are specified"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}.architecture('foo')", "machines.${currentHostOperatingSystemFamilyDsl}")
		buildFile << configureToolChainSupport(DefaultNativePlatform.currentOperatingSystem.toFamilyName(), 'foo')

		expect:
		succeeds taskNameToAssembleDevelopmentBinary
		assertComponentUnderTestWasBuilt(currentArchitecture)
	}

	def "ignores duplicate target machines"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}", "machines.${currentHostOperatingSystemFamilyDsl}")
		buildFile << """
            task verifyTargetMachineCount {
                doLast {
                    assert ${componentUnderTestDsl}.targetMachines.get().size() == 1
                    assert ${componentUnderTestDsl}.targetMachines.get() == [${componentUnderTestDsl}.machines.${currentHostOperatingSystemFamilyDsl}] as Set
                }
            }
        """

		expect:
		succeeds "verifyTargetMachineCount"
	}

	def "disallow target machines after configuration is done"() {
		makeSingleProject()
		buildFile << """
			tasks.register('modifyTargetMachines') {
				doLast {
					${configureTargetMachines()}
				}
			}
		"""

		expect:
		fails('modifyTargetMachines')
		failure.assertHasCause("The value for property 'targetMachines' is final and cannot be changed any further.")
	}

	def "can specify unbuildable architecture as a component target machine"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.${currentHostOperatingSystemFamilyDsl}", "machines.${currentHostOperatingSystemFamilyDsl}.architecture('foo')")
		buildFile << configureToolChainSupport(DefaultNativePlatform.currentOperatingSystem.toFamilyName(), 'foo')

		expect:
		succeeds taskNameToAssembleDevelopmentBinary
		assertComponentUnderTestWasBuilt(currentArchitecture)
	}

	protected abstract void makeSingleProject()

	protected String getComponentUnderTestDsl() {
		if (this.class.simpleName.contains('Application')) {
			return 'application'
		}
		return 'library'
	}

	protected abstract SourceElement getComponentUnderTest()

	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	protected List<String> getTasksToAssembleDevelopmentBinary() {
		return tasks.allToAssemble
	}

	protected String getTaskNameToAssembleDevelopmentBinaryWithArchitecture(String architecture) {
		return "assemble${architecture.capitalize()}"
	}

	protected String getComponentName() {
		return 'main'
	}

	protected String getProjectName() {
		if (this.class.simpleName.contains('Application')) {
			return 'application'
		}
		return 'library'
	}

	protected abstract void assertComponentUnderTestWasBuilt(String variant = '')

	protected String configureToolChainSupport(String operatingSystem, String architecture) {
		String className = "ToolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}Rules".replace('-', '')
		return """
			class ${className} extends RuleSource {
				@Finalize
				void addToolChain(NativeToolChainRegistry toolChains) {
					toolChains.create("toolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}", Gcc) {
						path "/not/found"
						target("${operatingSystem}${architecture}") // It needs to be the same as NativePlatformFactory#platformNameFor
					}
				}
			}
			import ${StandardToolChainsPlugin.canonicalName}
			plugins.withType(StandardToolChainsPlugin) {
				// TODO: Applies after the Stardard tool chain plugin
				plugins.apply(${className})
			}
		"""

		// The following replace the tool chains
//		return """
//            model {
//                toolChains {
//                    "toolChainFor${operatingSystem.capitalize()}${architecture.capitalize()}"(Gcc) {
//                        path "/not/found"
//                        target("${operatingSystem}${architecture}") // It needs to be the same as NativePlatformFactory#platformNameFor
//                    }
//                }
//            }
//        """
	}

	protected String getCurrentHostOperatingSystemFamilyDsl() {
		String osFamily = DefaultNativePlatform.getCurrentOperatingSystem().toFamilyName()
		if (osFamily == OperatingSystemFamily.MACOS) {
			return "macOS"
		} else {
			return osFamily
		}
	}

	protected configureTargetMachines(String... targetMachines) {
		return """
            ${componentUnderTestDsl} {
                targetMachines = [${targetMachines.join(",")}]
            }
        """
	}
}
