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
