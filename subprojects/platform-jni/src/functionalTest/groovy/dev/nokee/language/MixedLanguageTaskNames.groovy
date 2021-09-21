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
package dev.nokee.language

import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.language.java.JavaTaskNames

trait MixedLanguageTaskNames implements LanguageTaskNames {
	LanguageTaskNamesRegistry getTaskNames() {
		return new LanguageTaskNamesRegistry()
	}

	static class LanguageTaskNamesRegistry {
		JavaTaskNames getJava() {
			return [] as JavaTaskNames
		}

		CppTaskNames getCpp() {
			return [] as CppTaskNames
		}
	}

//	/**
//	 * Returns the tasks for the root project.
//	 */
//	ProjectTasks getTasks() {
//		return new ProjectTasks(''/*, toolchainUnderTest, languageTaskSuffix, additionalTestTaskNames*/)
//	}

//	static class ProjectTasks {
//		private final String project
//
//		ProjectTasks(String project) {
//			this.project = project
//		}
//	}
}
