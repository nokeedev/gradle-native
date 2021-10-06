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
package dev.nokee.platform.nativebase;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an native header set named {@literal headers}.
 *
 * @see ComponentSources
 * @see NativeHeaderSet
 * @since 0.5
 */
public interface HasHeadersSourceSet {
	/**
	 * Returns a native header set provider for the source set named {@literal headers}.
	 *
	 * @return a provider for {@literal headers} source set, never null
	 */
	default NamedDomainObjectProvider<NativeHeaderSet> getHeaders() {
		return ((FunctionalSourceSet) this).named("headers", NativeHeaderSet.class);
	}

	/**
	 * Configures the native header set named {@literal headers} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getHeaders()
	 */
	default void headers(Action<? super NativeHeaderSet> action) {
		getHeaders().configure(action);
	}

	/**
	 * Configures the native header set named {@literal headers} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getHeaders()
	 */
	default void headers(@DelegatesTo(value = NativeHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		headers(configureUsing(closure));
	}
}
