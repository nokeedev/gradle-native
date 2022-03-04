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
package dev.nokee.language.base;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.View;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.util.ConfigureUtil;

/**
 * A view of the language source sets with a similar function (production code, test code, etc.).
 *
 * @since 0.5
 */
public interface FunctionalSourceSet extends View<LanguageSourceSet> {
	/**
	 * Configures an element of the specified name.
	 *
	 * @param name  the name of the element to configure.
	 * @param action  the configuration action.
	 */
	void configure(String name, Action<? super LanguageSourceSet> action);

	/**
	 * Configures an element of the specified name.
	 *
	 * @param name  the name of the element to configure.
	 * @param closure  the configuration closure.
	 */
	default void configure(String name, @DelegatesTo(value = LanguageSourceSet.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
	}

	/**
	 * Configures an element of the specified name and type.
	 *
	 * @param name  the name of the element to configure.
	 * @param type  the type of element to configure.
	 * @param action  the configuration action.
	 * @param <S>  the type of the element to configure.
	 */
	<S extends LanguageSourceSet> void configure(String name, Class<S> type, Action<? super S> action);

	/**
	 * Configures an element of the specified name and type.
	 *
	 * @param name The name of the element to configure.
	 * @param type The type of element to configure.
	 * @param closure The configuration closure.
	 * @param <S> The type of the element to configure.
	 */
	default <S extends LanguageSourceSet> void configure(String name, Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		configure(name, type, ConfigureUtil.configureUsing(closure));
	}

	<S extends LanguageSourceSet> NamedDomainObjectProvider<S> named(String name, Class<S> type);

	/**
	 * {@inheritDoc}
	 */
	<S extends LanguageSourceSet> View<S> withType(Class<S> type);

	/**
	 * Executes given action when an element becomes known to the view.
	 *
	 * @param action  the action to execute for each known element of this view
	 */
	void whenElementKnown(Action<? super KnownDomainObject<LanguageSourceSet>> action);

	/**
	 * Executes given closure when an element becomes known to the view of the specified type.
	 *
	 * @param closure  the closure to execute for each known element of this view
	 */
	void whenElementKnown(@DelegatesTo(value = KnownDomainObject.class, strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);

	/**
	 * Executes given action when an element becomes known to the view of the specified type.
	 *
	 * @param type  the element type to match
	 * @param action  the action to execute for each known element of this view
	 * @param <S>  the element type to match
	 */
	<S extends LanguageSourceSet> void whenElementKnown(Class<S> type, Action<? super KnownDomainObject<S>> action);

	/**
	 * Executes given closure when an element becomes known to the view of the specified type.
	 *
	 * @param type  the element type to match
	 * @param closure  the closure to execute for each known element of this view
	 * @param <S>  the element type to match
	 */
	<S extends LanguageSourceSet> void whenElementKnown(Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure);
}
