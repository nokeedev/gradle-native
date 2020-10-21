package dev.nokee.fixtures

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.fixtures.sources.SourceElement

abstract class AbstractNativeApplicationComponentFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def "can run application from command line"() {
		given:
		settingsFile << 'rootProject.name = "application"'
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('assemble')

		then:
		executable('build/exes/main/application').assertExists()
		executable('build/exes/main/application').exec().out == 'Bonjour, Alice!\n'
	}

	protected abstract void makeSingleProject()

	protected abstract SourceElement getComponentUnderTest()
}
