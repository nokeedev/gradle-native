package dev.nokee.ide.visualstudio.fixtures

import dev.nokee.ide.fixtures.IdeProjectTasks
import dev.nokee.ide.fixtures.IdeTaskNames
import dev.nokee.ide.fixtures.IdeWorkspaceTasks

trait VisualStudioIdeTaskNames implements IdeTaskNames {
	@Override
	IdeProjectTasks tasks(String project) {
		return new VisualStudioIdeProjectTasks(project)
	}

	@Override
	IdeWorkspaceTasks getTasks() {
		return new VisualStudioIdeSolutionTasks()
	}

	static class VisualStudioIdeProjectTasks implements IdeProjectTasks {
		private final String project

		VisualStudioIdeProjectTasks(String project) {
			this.project = project
		}

		String withProject(String t) {
			return project + ":" + t
		}

		@Override
		String getIdeLifecycle() {
			return withProject('visualStudio')
		}

		@Override
		String getIdeClean() {
			return withProject('cleanVisualStudio')
		}

		@Override
		String ideProject(String name) {
			return withProject("${name}VisualStudioProject")
		}
	}

	static class VisualStudioIdeSolutionTasks extends VisualStudioIdeProjectTasks implements IdeWorkspaceTasks {
		VisualStudioIdeSolutionTasks() {
			super('')
		}

		@Override
		List<String> getAllToIde() {
			return [ideLifecycle, ideWorkspace]
		}

		@Override
		String getIdeWorkspace() {
			return withProject('visualStudioSolution')
		}
	}
}
