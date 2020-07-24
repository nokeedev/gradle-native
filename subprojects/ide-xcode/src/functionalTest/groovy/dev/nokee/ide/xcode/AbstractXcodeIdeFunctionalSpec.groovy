package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.ide.fixtures.IdeCommandLineUtils
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.ide.xcode.fixtures.XcodebuildExecutor

class AbstractXcodeIdeFunctionalSpec extends AbstractGradleSpecification implements XcodeIdeFixture {
	protected XcodeIdeWorkspaceFixture xcodeWorkspace(String path) {
		return XcodeIdeWorkspaceFixture.of(file(path))
	}

	protected XcodeIdeWorkspaceFixture getXcodeWorkspace() {
		return xcodeWorkspace(testDirectory.name)
	}

	protected XcodeIdeProjectFixture xcodeProject(String path) {
		return XcodeIdeProjectFixture.of(file(path))
	}

	protected XcodeIdeProjectFixture getXcodeProject() {
		return xcodeProject(testDirectory.name)
	}

	protected XcodebuildExecutor getXcodebuild() {
		// TODO: Ensure the following
//		// Gradle needs to be isolated so the xcodebuild does not leave behind daemons
//		assert executer.isRequiresGradleDistribution()
//		assert !executer.usesSharedDaemons()
		new XcodebuildExecutor(testDirectory)
	}

	void useXcodebuildTool() {
		// TODO: Ensure the following
//		executer.requireIsolatedDaemons()

		def initScript = file("init.gradle")
		initScript << IdeCommandLineUtils.generateGradleProbeInitFile('xcode', 'xcodebuild')
		executer = executer.usingInitScript(initScript)
	}
}
