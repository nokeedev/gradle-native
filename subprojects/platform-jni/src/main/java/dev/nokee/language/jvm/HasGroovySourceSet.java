/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.jvm;

import dev.nokee.platform.base.ComponentSources;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

/**
 * Represents a component sources that carries a Groovy source set named {@literal groovy}.
 *
 * @see ComponentSources
 * @see GroovySourceSet
 * @since 0.5
 */
public interface HasGroovySourceSet {
	/**
	 * Returns a Groovy source set provider for the source set named {@literal groovy}.
	 *
	 * @return a provider for {@literal groovy} source set, never null
	 */
	NamedDomainObjectProvider<GroovySourceSet> getGroovy();

	/**
	 * Configures the Groovy source set named {@literal groovy} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getGroovy()
	 */
	void groovy(Action<? super GroovySourceSet> action);
}
