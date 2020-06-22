package dev.nokee.language

trait NativeLanguageTaskNames implements NativeProjectTaskNames {
	abstract String getLanguageTaskSuffix()

	/**
	 * Returns the tasks for the project with the given path.
	 */
	NativeProjectTasks tasks(String project) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix)
	}

	/**
	 * Returns the tasks for the root project.
	 */
	NativeProjectTasks getTasks() {
		return new DefaultNativeProjectTasks('', languageTaskSuffix)
	}
}
