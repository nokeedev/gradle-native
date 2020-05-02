package dev.nokee.ide.xcode

import spock.lang.Unroll

class XcodeIdeGradleTargetsFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	protected void makeProjectWithSubproject() {
		settingsFile << """
			rootProject.name = 'foo'
			include 'bar'
		"""
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')
		file('bar/build.gradle') << applyXcodeIdePlugin() << configureXcodeIdeProject('bar')
	}

	def "includes init script from the command line in all Xcode Gradle target command line"() {
		makeProjectWithSubproject()
		def initFile = file('init.gradle')
		initFile.createNewFile()

		when:
		executer = executer.usingInitScript(initFile)
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(':bar:barXcodeProject', ':bar:xcode', ':fooXcodeProject', ':xcodeWorkspace', ':xcode')
		xcodeProject('foo').getTargetByName('Foo').assertTargetDelegateToGradle()
		xcodeProject('foo').getTargetByName('Foo').buildArgumentsString.contains("--init-script \"${initFile}\"")
		xcodeProject('bar/bar').getTargetByName('Bar').buildArgumentsString.contains("--init-script \"${initFile}\"")
	}

	@Unroll
	def "delegates to Gradle for all product types (#productType)"(productType) {
		given:
		settingsFile << "rootProject.name = 'foo'"
		buildFile << applyXcodeIdePlugin() << configureXcodeIdeProject('foo')

		when:
		succeeds('xcode')

		then:
		xcodeProject('foo').getTargetByName('Foo').assertTargetDelegateToGradle()

		where:
		productType << XcodeIdeProductTypes.getKnownValues()
	}
}
