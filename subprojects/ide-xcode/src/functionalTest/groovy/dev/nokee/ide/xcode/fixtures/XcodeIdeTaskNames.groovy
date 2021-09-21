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
package dev.nokee.ide.xcode.fixtures

import dev.nokee.ide.fixtures.IdeProjectTasks
import dev.nokee.ide.fixtures.IdeTaskNames
import dev.nokee.ide.fixtures.IdeWorkspaceTasks

trait XcodeIdeTaskNames implements IdeTaskNames {
	@Override
	IdeProjectTasks tasks(String project) {
		return new XcodeIdeProjectTasks(project)
	}

	@Override
	IdeWorkspaceTasks getTasks() {
		return new XcodeIdeWorkspaceTasks()
	}

	static class XcodeIdeProjectTasks implements IdeProjectTasks {
		private final String project

		XcodeIdeProjectTasks(String project) {
			this.project = project
		}

		String withProject(String t) {
			return project + ":" + t
		}

		@Override
		String getIdeLifecycle() {
			return withProject('xcode')
		}

		@Override
		String getIdeClean() {
			return withProject('cleanXcode')
		}

		@Override
		String ideProject(String name) {
			return withProject("${name}XcodeProject")
		}
	}

	static class XcodeIdeWorkspaceTasks extends XcodeIdeProjectTasks implements IdeWorkspaceTasks {
		XcodeIdeWorkspaceTasks() {
			super('')
		}

		@Override
		String getIdeWorkspace() {
			return withProject('xcodeWorkspace')
		}
	}
}
