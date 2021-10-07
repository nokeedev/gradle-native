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

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an Objective-C source set named {@literal objectiveC}.
 *
 * @see ComponentSources
 * @see ObjectiveCSourceSet
 * @since 0.5
 */
public interface HasObjectiveCSourceSet {
	/**
	 * Returns a Objective-C source set provider for the source set named {@literal objectiveC}.
	 *
	 * @return a provider for {@literal objectiveC} source set, never null
	 */
	default NamedDomainObjectProvider<ObjectiveCSourceSet> getObjectiveC() {
		return ((FunctionalSourceSet) this).named("objectiveC", ObjectiveCSourceSet.class);
	}

	/**
	 * Configures the Objective-C source set named {@literal objectiveC} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveC()
	 */
	default void objectiveC(Action<? super ObjectiveCSourceSet> action) {
		getObjectiveC().configure(action);
	}

	/**
	 * Configures the Objective-C source set named {@literal objectiveC} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getObjectiveC()
	 */
	default void objectiveC(@DelegatesTo(value = ObjectiveCSourceSet.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		objectiveC(configureUsing(closure));
	}
}
