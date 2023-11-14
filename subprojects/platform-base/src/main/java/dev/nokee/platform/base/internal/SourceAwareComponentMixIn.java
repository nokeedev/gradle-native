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
package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.plugins.ExtensionAware;

public interface SourceAwareComponentMixIn<T extends ComponentSources, S extends T> extends SourceAwareComponent<T>, ExtensionAware {
	@Override
	@SuppressWarnings("unchecked")
	default T getSources() {
		return (T) getExtensions().getByName("sources");
	}

	@Override
	default void sources(Action<? super T> action) {
		action.execute(getSources());
	}

	@Override
	default void sources(@SuppressWarnings("rawtypes") Closure closure) {
		sources(new ClosureWrappedConfigureAction<>(closure));
	}
}
