package dev.nokee.ide.visualstudio

import dev.nokee.ide.fixtures.AbstractIdeWorkspaceFunctionalTest
import dev.nokee.ide.fixtures.IdeWorkspaceFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeSolutionFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

class VisualStudioIdeWorkspaceFunctionalTest extends AbstractIdeWorkspaceFunctionalTest implements VisualStudioIdeFixture, VisualStudioIdeTaskNames {
	// TODO: Can build empty IDE solution with MSBuild
	// TODO: Can build non-empty IDE solution with MSBuild

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected String getIdeUnderTestDsl() {
		return 'visualStudio'
	}

	@Override
	protected String workspaceName(String name) {
		return VisualStudioIdeSolutionFixture.solutionName(name)
	}

	@Override
	protected IdeWorkspaceFixture getIdeWorkspaceUnderTest() {
		return visualStudioSolution(rootProjectName)
	}

	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}
}
