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

import dev.nokee.ide.fixtures.AbstractIdeLifecycleTasksFunctionalTest
import dev.nokee.ide.xcode.fixtures.XcodeIdeTaskNames
import dev.nokee.ide.xcode.fixtures.XcodeIdeWorkspaceFixture

class XcodeIdeLifecycleTasksFunctionalTest extends AbstractIdeLifecycleTasksFunctionalTest implements XcodeIdeFixture, XcodeIdeTaskNames {
	@Override
	protected String getIdeWorkspaceDisplayNameUnderTest() {
		return 'Xcode workspace'
	}

	@Override
	protected String workspaceName(String name) {
		return XcodeIdeWorkspaceFixture.workspaceName(name)
	}

	@Override
	protected String getIdeUnderTestDsl() {
		return 'xcode'
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureXcodeIdeProject(name)
	}

	@Override
	protected String getIdePluginId() {
		return xcodeIdePluginId
	}
}
