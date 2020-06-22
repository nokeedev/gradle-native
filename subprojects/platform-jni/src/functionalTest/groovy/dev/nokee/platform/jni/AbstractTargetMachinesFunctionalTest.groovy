package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.containsString
// See https://github.com/gradle/gradle-native/issues/982 for the distinction between unknown, unsupported and unbuildable.
abstract class AbstractTargetMachinesFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
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
	def "#taskName task warns when current operating system family is excluded"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureToolChainSupport('some-other-family', currentArchitecture)
		buildFile << configureTargetMachines("machines.os('some-other-family')")

		expect:
		succeeds taskNameToAssembleDevelopmentBinary

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
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << configureTargetMachines("machines.os('some-other-family')")

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
		failure.assertHasCause("A target machine needs to be specified for the library.")
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
		succeeds getTaskNameToAssembleDevelopmentBinaryWithArchitecture(DefaultMachineArchitecture.X86.name), getTaskNameToAssembleDevelopmentBinaryWithArchitecture(DefaultMachineArchitecture.X86_64.name)
		assertComponentUnderTestWasBuilt(DefaultMachineArchitecture.X86.name)
		assertComponentUnderTestWasBuilt(DefaultMachineArchitecture.X86_64.name)
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
                    assert ${componentUnderTestDsl}.targetMachines.get() == [library.machines.${currentHostOperatingSystemFamilyDsl}] as Set
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
		println testDirectory
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

	protected abstract String getComponentUnderTestDsl()

	protected abstract SourceElement getComponentUnderTest()

	protected abstract String getTaskNameToAssembleDevelopmentBinary()

	protected abstract List<String> getTasksToAssembleDevelopmentBinary()

	protected abstract String getTaskNameToAssembleDevelopmentBinaryWithArchitecture(String architecture)

	protected abstract String getComponentName()

	protected abstract String getProjectName()

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
			// TODO: Applies after the Stardard tool chain plugin
			apply plugin: ${className}
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
