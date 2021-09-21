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

import dev.nokee.ide.fixtures.AbstractIdeGradleBuildFilesFunctionalTest
import dev.nokee.ide.fixtures.IdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeTaskNames

class VisualStudioIdeGradleBuildFilesFunctionalTest extends AbstractIdeGradleBuildFilesFunctionalTest implements VisualStudioIdeTaskNames, VisualStudioIdeFixture {
	@Override
	protected String getIdePluginId() {
		return visualStudioIdePluginId
	}

	@Override
	protected String configureIdeProject(String name) {
		return configureVisualStudioIdeProject(name)
	}

	@Override
	protected IdeProjectFixture ideProject(String name) {
		return VisualStudioIdeProjectFixture.of(file(name))
	}
}
