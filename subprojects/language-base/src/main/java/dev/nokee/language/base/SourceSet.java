/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.base;

import org.gradle.api.Buildable;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;

/**
 * Represent a logical grouping of several files or directories.
 *
 * WARNING: It is not to be confused with Gradle's {@link org.gradle.api.tasks.SourceSet} which is closer to Nokee's {@link LanguageSourceSet}.
 * @since 0.5
 */
public interface SourceSet extends Buildable {
	/**
	 * Returns this source set as a filtered file tree.
	 *
	 * @return a {@link FileTree} instance representing all the files included in this source set, never null
	 */
	FileTree getAsFileTree();

	/**
	 * Returns the source directories that make up this set, represented as a {@link FileCollection}.
	 * Does not filter source directories that do not exist.
	 *
	 * <p>The return value of this method also maintains dependency information.
	 *
	 * <p>The returned collection is live and reflects changes to this source directory set.
	 *
	 * @return a {@link FileCollection} instance of all the source directories from this source set, never null
	 */
	FileCollection getSourceDirectories();
}
