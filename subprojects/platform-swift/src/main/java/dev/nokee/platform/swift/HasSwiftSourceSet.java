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
package dev.nokee.platform.swift;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an Swift source set named {@literal swift}.
 *
 * @see ComponentSources
 * @see SwiftSourceSet
 * @since 0.5
 */
public interface HasSwiftSourceSet {
	/**
	 * Returns a Swift source set provider for the source set named {@literal swift}.
	 *
	 * @return a provider for {@literal swift} source set, never null
	 */
	default DomainObjectProvider<SwiftSourceSet> getSwift() {
		return ((FunctionalSourceSet) this).get("swift", SwiftSourceSet.class);
	}

	/**
	 * Configures the Swift source set named {@literal swift} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getSwift()
	 */
	default void swift(Action<? super SwiftSourceSet> action) {
		getSwift().configure(action);
	}

	/**
	 * Configures the Swift source set named {@literal swift} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getSwift()
	 */
	default void swift(@DelegatesTo(value = SwiftSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		swift(configureUsing(closure));
	}
}
