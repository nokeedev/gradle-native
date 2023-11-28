/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.base.internal.mixins;

import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;

public interface DependencyAwareComponentMixIn<T extends ComponentDependencies, U extends T> extends DependencyAwareComponent<T> {
	@Override
	@NestedObject
	U getDependencies();

	@Override
	default void dependencies(Action<? super T> action) {
		action.execute(getDependencies());
	}

	@Override
	default void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		dependencies(new ClosureWrappedConfigureAction<>(closure));
	}
}
