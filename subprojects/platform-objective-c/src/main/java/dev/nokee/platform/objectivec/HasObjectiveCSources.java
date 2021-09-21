/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.objectivec;

import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries Objective-C sources.
 *
 * @see HasObjectiveCSourceSet
 * @since 0.5
 */
public interface HasObjectiveCSources {
	/**
	 * Defines the Objective-C sources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/objectiveC} (and {@code src/componentName/objc} for backward compatibility), where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the Objective-C sources of this component, never null
	 * @see ObjectiveCSourceSet
	 * @since 0.5
	 */
	default ObjectiveCSourceSet getObjectiveCSources() {
		return ((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().get();
	}

	/**
	 * Configures the Objective-C sources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveCSources()
	 */
	default void objectiveCSources(Action<? super ObjectiveCSourceSet> action) {
		((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().configure(action);
	}

	/**
	 * Configures the Objective-C sources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getObjectiveCSources()
	 */
	default void objectiveCSources(@DelegatesTo(value = ObjectiveCSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCSources(configureUsing(closure));
	}
}
