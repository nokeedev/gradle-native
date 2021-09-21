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
