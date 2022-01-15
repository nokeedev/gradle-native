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
package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

/**
 * A component with dependency buckets.
 *
 * @param <T> type of the component dependencies
 * @since 0.4
 */
public interface DependencyAwareComponent<T extends ComponentDependencies> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link ComponentDependencies}, never null.
	 */
	T getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link ComponentDependencies}.
	 */
	void dependencies(Action<? super T> action);
	void dependencies(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}

