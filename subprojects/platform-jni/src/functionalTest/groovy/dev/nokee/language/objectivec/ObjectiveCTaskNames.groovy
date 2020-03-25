package dev.nokee.language.objectivec

import dev.nokee.language.LanguageTaskNames

trait ObjectiveCTaskNames implements LanguageTaskNames {
	/**
	 * Returns the tasks for the root project.
	 */
	ProjectTasks getTasks() {
		return new ProjectTasks('')
	}

	static class ProjectTasks {
		private final String project

		ProjectTasks(String project) {
			this.project = project
		}

		String getCompile() {
			return withProject("compileMainSharedLibraryMainObjc")
		}

		String getLink() {
			return withProject("linkMainSharedLibrary")
		}

		String getAssemble() {
			return withProject("assemble")
		}

		private String getSharedLibrary() {
			return withProject("mainSharedLibrary")
		}

		List<String> getAllToLink() {
			return [compile, link]
		}

		List<String> getAllToSharedLibrary() {
			return allToLink + [sharedLibrary]
		}

		List<String> getAllToAssemble() {
			return allToSharedLibrary + [assemble]
		}

		private withProject(String t) {
			project + ":" + t
		}
	}
}
