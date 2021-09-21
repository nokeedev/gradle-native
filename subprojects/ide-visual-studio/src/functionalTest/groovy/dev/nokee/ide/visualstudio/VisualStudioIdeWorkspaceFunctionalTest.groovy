/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
