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
package dev.nokee.platform.nativebase;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;

public interface NativeBinary extends Binary {
	/**
	 * Returns a view of all compile tasks that participate to compiling all the object files for this binary.
	 *
	 * @return a view of {@link SourceCompile} tasks, never null.
	 */
	TaskView<SourceCompile> getCompileTasks();

	/**
	 * Returns whether this binary can be built in the current environment.
	 *
	 * @return {@code true} if this binary can be built in the current environment or {@code false} otherwise.
	 */
	boolean isBuildable();
}
