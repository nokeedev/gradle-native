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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;

final class DefaultConfigurableSourceSet implements ConfigurableSourceSet {
	private final LanguageSourceSetStrategy strategy;

	public DefaultConfigurableSourceSet(LanguageSourceSetStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public PatternFilterable getFilter() {
		return strategy.getFilter();
	}

	@Override
	public ConfigurableSourceSet filter(Action<? super PatternFilterable> action) {
		action.execute(strategy.getFilter());
		return this;
	}

	@Override
	public ConfigurableSourceSet from(Object... paths) {
		strategy.from(paths);
		return this;
	}

	@Override
	public void setFrom(Object... paths) {
		strategy.setFrom(paths);
	}

	@Override
	public ConfigurableSourceSet convention(Object... paths) {
		strategy.convention(paths);
		return this;
	}

	@Override
	public FileTree getAsFileTree() {
		return strategy.getAsFileTree();
	}

	@Override
	public FileCollection getSourceDirectories() {
		return strategy.getSourceDirectories();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return strategy.getBuildDependencies();
	}
}
