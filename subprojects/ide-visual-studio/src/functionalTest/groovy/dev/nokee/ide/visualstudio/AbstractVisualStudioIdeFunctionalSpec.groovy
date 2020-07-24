package dev.nokee.ide.visualstudio

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.ide.fixtures.IdeCommandLineUtils
import dev.nokee.ide.visualstudio.fixtures.MSBuildExecutor
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture

class AbstractVisualStudioIdeFunctionalSpec extends AbstractGradleSpecification {
	protected VisualStudioIdeSolutionFixture visualStudioSolution(String path) {
		return VisualStudioIdeSolutionFixture.of(file(path))
	}

	protected VisualStudioIdeSolutionFixture getVisualStudioSolution() {
		return visualStudioSolution(testDirectory.name)
	}

	protected VisualStudioIdeProjectFixture visualStudioProject(String path) {
		return VisualStudioIdeProjectFixture.of(file(path))
	}

	protected VisualStudioIdeProjectFixture getVisualStudioProject() {
		return visualStudioProject(testDirectory.name)
	}

	protected MSBuildExecutor getMsbuild() {
		// TODO: Ensure the following
//		// Gradle needs to be isolated so the xcodebuild does not leave behind daemons
//		assert executer.isRequiresGradleDistribution()
//		assert !executer.usesSharedDaemons()
		return new MSBuildExecutor(testDirectory)
	}

	void useMSBuildTool() {
		// TODO: Ensure the following
//		executer.requireIsolatedDaemons()

		def initScript = file("init.gradle")
		initScript << IdeCommandLineUtils.generateGradleProbeInitFile('visualStudio', 'msbuild')
		executer = executer.usingInitScript(initScript)
	}
}
