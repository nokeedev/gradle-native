package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.AvailableToolChains
import dev.gradleplugins.test.fixtures.sources.SourceElement

abstract class AbstractSharedLibraryConfigurationFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can define compiler flags from the model"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.compileTasks.configureEach {
					compilerArgs.add('-DWITH_FEATURE')
				}
			}
		"""

		when:
		executer = executer.withArgument('-i')
		succeeds('assemble')

		then:
		outputContains('compiling with feature enabled')

		and:
		// TODO: Create the symetrie of linkerOptionsFor(...) inside AbstractInstalledToolChainIntegrationSpec
		file("build/tmp/${compileTaskName}/options.txt").text.contains('-DWITH_FEATURE')
	}

	protected abstract String getCompileTaskName()

	def "can define linker flags from the model"() {
		given:
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		and:
		buildFile << """
			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.addAll(toolChain.map {
						if (it in VisualCpp) {
							return ['/Wall']
						}
						return ['-Werror']
					});
				}
			}
		"""

		when:
		executer = executer.withArgument('-i')
		succeeds('assemble')

		then:
		String expectedFlag = '-Werror'
		if (toolchainUnderTest.family == AvailableToolChains.ToolFamily.VISUAL_CPP) {
			expectedFlag = '/Wall'
		}
		linkerOptionsFor('linkMainSharedLibrary').options.text.contains(expectedFlag)
	}

	protected abstract void makeSingleProject();

	protected abstract SourceElement getComponentUnderTest();
}
