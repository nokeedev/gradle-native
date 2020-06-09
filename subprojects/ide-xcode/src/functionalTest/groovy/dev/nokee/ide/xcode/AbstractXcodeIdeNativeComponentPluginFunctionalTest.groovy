package dev.nokee.ide.xcode

import dev.gradleplugins.test.fixtures.sources.SourceElement
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

abstract class AbstractXcodeIdeNativeComponentPluginFunctionalTest extends AbstractXcodeIdeFunctionalSpec {
	protected abstract void makeSingleProject();

	protected abstract SourceElement getComponentUnderTest();

	protected abstract String getProjectName();

	protected String getSchemeName() {
		return projectName
	}

	protected String getWorkspaceName() {
		return projectName
	}

	protected abstract List<String> getAllTasksForBuildAction()

	@Requires({ SystemUtils.IS_OS_MAC })
	def "can build from Xcode IDE"() {
		useXcodebuildTool()
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << """
			rootProject.name = '${projectName}'
		"""
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		succeeds('xcode')

		then:
		result.assertTasksExecutedAndNotSkipped(":${projectName}XcodeProject", ':xcodeWorkspace', ':xcode')

		and:
		def result = xcodebuild.withWorkspace(xcodeWorkspace(workspaceName)).withScheme(schemeName).succeeds()
		result.assertTasksExecuted(allTasksForBuildAction, ":_xcode___${projectName}_${schemeName}_Default")

		and:
		if (!componentUnderTest.files.empty) {
			result.assertTasksNotSkipped(allTasksForBuildAction, ":_xcode___${projectName}_${schemeName}_Default")
		}
	}
}
