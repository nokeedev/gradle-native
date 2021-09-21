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
package dev.nokee.ide.xcode

import dev.nokee.ide.fixtures.AbstractIdeWorkspaceFunctionalTest
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture
import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires

class XcodeIdeWorkspaceFunctionalTest extends AbstractIdeWorkspaceFunctionalTest implements XcodeIdeFixture, XcodeIdeTaskNames {
	@Requires({ SystemUtils.IS_OS_MAC })
	def "xcodebuild sees no schemes inside empty workspace"() {
		using getXcodebuildTool('xcodeWorkspace')

		given:
		settingsFile << configurePluginClasspathAsBuildScriptDependencies() << "rootProject.name = 'app'"
		buildFile << applyXcodeIdePlugin()

		when:
		succeeds(tasks.ideWorkspace)

		then:
		def result = xcodebuild.withWorkspace(xcodeWorkspace('app')).withArgument('-list').execute()
		result.standardOutput.asString.contains('There are no schemes in workspace "app".')
	}

	def "relocates Xcode derived data into root project's build folder"() {
		given:
		settingsFile << "rootProject.name = 'app'"
		buildFile << applyXcodeIdePlugin()

		when:
		succeeds(tasks.ideWorkspace)

		then:
		xcodeWorkspace('app').assertDerivedDataLocationRelativeToWorkspace('.gradle/XcodeDerivedData')
	}

	// TODO: Can build non-empty IDE workspace with xcodebuild

	@Override
	protected String configureIdeProject(String name) {
		return configureXcodeIdeProject(name)
	}

	@Override
	protected String getIdeUnderTestDsl() {
		return 'xcode'
	}

	@Override
	protected String workspaceName(String name) {
		return XcodeIdeWorkspaceFixture.workspaceName(name)
	}

	@Override
	protected XcodeIdeWorkspaceFixture getIdeWorkspaceUnderTest() {
		return xcodeWorkspace(rootProjectName)
	}

	@Override
	protected String getIdePluginId() {
		return xcodeIdePluginId
	}
}
