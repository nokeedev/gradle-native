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
package dev.nokee.platform.c;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an C source set named {@literal c}.
 *
 * @see ComponentSources
 * @see CSourceSet
 * @since 0.5
 */
public interface HasCSourceSet {
	/**
	 * Returns a C source set provider for the source set named {@literal c}.
	 *
	 * @return a provider for {@literal c} source set, never null
	 */
	default NamedDomainObjectProvider<CSourceSet> getC() {
		return ((FunctionalSourceSet) this).named("c", CSourceSet.class);
	}

	/**
	 * Configures the C source set named {@literal c} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getC()
	 */
	default void c(Action<? super CSourceSet> action) {
		getC().configure(action);
	}

	/**
	 * Configures the C source set named {@literal c} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getC()
	 */
	default void c(@DelegatesTo(value = CSourceSet.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		c(configureUsing(closure));
	}
}
