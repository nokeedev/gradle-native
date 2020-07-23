package dev.nokee.ide.visualstudio

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
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

//	protected XcodebuildExecutor getXcodebuild() {
//		// TODO: Ensure the following
////		// Gradle needs to be isolated so the xcodebuild does not leave behind daemons
////		assert executer.isRequiresGradleDistribution()
////		assert !executer.usesSharedDaemons()
//		new XcodebuildExecutor(testDirectory)
//	}
//
//	void useXcodebuildTool() {
//		// TODO: Ensure the following
////		executer.requireIsolatedDaemons()
//
//		def initScript = file("init.gradle")
//		initScript << IdeCommandLineUtil.generateGradleProbeInitFile('xcode', 'xcodebuild')
//		executer = executer.usingInitScript(initScript)
//	}
}
