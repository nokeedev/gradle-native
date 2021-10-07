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

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an iOS resource set named {@literal resources}.
 *
 * @see ComponentSources
 * @see IosResourceSet
 * @since 0.5
 */
public interface HasResourcesSourceSet {
	/**
	 * Returns an iOS resource set provider for the source set named {@literal resources}.
	 *
	 * @return a provider for {@literal resources} source set, never null
	 */
	default NamedDomainObjectProvider<IosResourceSet> getResources() {
		return ((FunctionalSourceSet) this).named("resources", IosResourceSet.class);
	}

	/**
	 * Configures the iOS resource set named {@literal resources} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(Action<? super IosResourceSet> action) {
		getResources().configure(action);
	}

	/**
	 * Configures the iOS resource set named {@literal resources} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getResources()
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		resources(configureUsing(closure));
	}
}
