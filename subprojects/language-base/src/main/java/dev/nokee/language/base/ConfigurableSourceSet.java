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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.util.ConfigureUtil;

/**
 * Represent a configurable logical grouping of several files or directories.
 *
 * WARNING: It is not to be confused with Gradle's {@link org.gradle.api.tasks.SourceSet} which is closer to Nokee's {@link LanguageSourceSet}.
 * @since 0.5
 */
public interface ConfigurableSourceSet extends SourceSet {
	/**
	 * Returns the filter used to select the source from the source directories.
	 * These filter patterns are applied after the include and exclude patterns of the specified paths.
	 * Generally, the filter patterns are used to restrict the contents to certain types of files, eg {@code *.cpp}.
	 *
	 * @return the filter patterns, never null
	 */
	PatternFilterable getFilter();

	/**
	 * Configures the filter patterns using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @return this language source set, never null
	 */
	ConfigurableSourceSet filter(Action<? super PatternFilterable> action);
	default ConfigurableSourceSet filter(@ClosureParams(value = SimpleType.class, options = "org.gradle.api.tasks.util.PatternFilterable") @DelegatesTo(value = PatternFilterable.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		return filter(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Adds a set of source paths to this source set.
	 * The given paths are evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param paths  the files to add
	 * @return this source set, never null
	 */
	ConfigurableSourceSet from(Object... paths);
	void setFrom(Object... paths);

	/**
	 * Configures a set of source paths to use as a convention of this source set.
	 * The given paths are evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param paths  the files to use as convention
	 * @return this source set, never null
	 */
	ConfigurableSourceSet convention(Object... paths);
}
