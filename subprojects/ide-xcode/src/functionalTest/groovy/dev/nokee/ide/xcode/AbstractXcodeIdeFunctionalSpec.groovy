package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.ide.xcode.fixtures.IdeCommandLineUtil
import dev.nokee.ide.xcode.fixtures.XcodeIdeProjectFixture
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import dev.nokee.ide.xcode.fixtures.XcodebuildExecutor

class AbstractXcodeIdeFunctionalSpec extends AbstractGradleSpecification implements XcodeIdeFixture {
	protected XcodeIdeWorkspaceFixture xcodeWorkspace(String path) {
		if (!path.endsWith('.xcworkspace')) {
			path = path + '.xcworkspace'
		}
		return new XcodeIdeWorkspaceFixture(file(path))
	}

	protected XcodeIdeWorkspaceFixture getXcodeWorkspace() {
		return xcodeWorkspace(testDirectory.name)
	}

	protected XcodeIdeProjectFixture xcodeProject(String path) {
		if (!path.endsWith('.xcodeproj')) {
			path = path + '.xcodeproj'
		}
		return new XcodeIdeProjectFixture(file(path))
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
		initScript << IdeCommandLineUtil.generateGradleProbeInitFile('xcode', 'xcodebuild')
		executer = executer.usingInitScript(initScript)
	}
}
