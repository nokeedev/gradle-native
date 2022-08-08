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
package dev.nokee.platform.ios;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries iOS resources.
 *
 * @since 0.5
 */
public interface HasIosResources {
	/**
	 * Defines the iOS resources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/resources}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the iOS resources of this component, never null
	 * @see IosResourceSet
	 */
	default IosResourceSet getResources() {
		return sourceViewOf(this).named("resources", IosResourceSet.class).get();
	}

	/**
	 * Configures the iOS resources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(Action<? super IosResourceSet> action) {
		sourceViewOf(this).named("resources", IosResourceSet.class).configure(action);
	}

	/**
	 * Configures the iOS resources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getResources()
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		resources(configureUsing(closure));
	}
}
