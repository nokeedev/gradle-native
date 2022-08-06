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
package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.nokee.utils.FileCollectionUtils.sourceDirectories;

final class DefaultLanguageSourceSetStrategy implements LanguageSourceSetStrategy {
	private final ConfigurableFileCollection sources;
	private final ConfigurableFileCollection sourceDirectories;
	private final ObjectFactory objectFactory;
	private final PatternSet patternSet = new PatternSet();
	private FileCollection conventionSources;
	private boolean hasValue = false;

	public DefaultLanguageSourceSetStrategy(ObjectFactory objectFactory) {
		this.sources = objectFactory.fileCollection().from(conventionIfSourceAbsent());
		this.sourceDirectories = objectFactory.fileCollection().from(sourcesDeduction());
		this.objectFactory = objectFactory;
	}

	public void from(Object... paths) {
		hasValue = hasValue || paths.length > 0;
		sources.from(paths);
	}

	public void setFrom(Object... paths) {
		hasValue = paths.length > 0;
		sources.setFrom(conventionIfSourceAbsent());
		sources.from(paths);
	}

	public void convention(Object... paths) {
		this.conventionSources = objectFactory.fileCollection().from(paths);
	}

	public FileCollection getSourceDirectories() {
		return sourceDirectories;
	}

	public PatternFilterable getFilter() {
		return patternSet;
	}

	public FileTree getAsFileTree() {
		return sources.getAsFileTree().matching(patternSet);
	}

	public TaskDependency getBuildDependencies() {
		return sources.getBuildDependencies();
	}

	private Callable<List<FileCollection>> conventionIfSourceAbsent() {
		return () -> {
			if (hasValue || conventionSources == null) {
				return Collections.emptyList();
			}
			return ImmutableList.of(conventionSources);
		};
	}

	private Callable<FileCollection> sourcesDeduction() {
		return () -> objectFactory.fileCollection().from(sourceDirectories(sources));
	}
}
