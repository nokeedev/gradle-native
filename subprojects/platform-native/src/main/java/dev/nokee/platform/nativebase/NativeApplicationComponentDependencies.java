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

import dev.nokee.platform.base.ApplicationComponentDependencies;
import dev.nokee.platform.base.ComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Allows the implementation dependencies of a native application to be specified.
 *
 * @since 0.5
 */
public interface NativeApplicationComponentDependencies extends ApplicationComponentDependencies, NativeComponentDependencies, ComponentDependencies {
	/**
	 * {@inheritDoc}
	 */
	@Override
	default void implementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}
}
