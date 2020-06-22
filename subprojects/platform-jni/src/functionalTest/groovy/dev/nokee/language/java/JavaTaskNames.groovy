package dev.nokee.language.java

import dev.nokee.language.DefaultJavaProjectTasks
import dev.nokee.language.LanguageTaskNames

trait JavaTaskNames implements LanguageTaskNames {
	/**
	 * Returns the tasks for the root project.
	 */
	DefaultJavaProjectTasks getTasks() {
		return new DefaultJavaProjectTasks('')
	}
}
