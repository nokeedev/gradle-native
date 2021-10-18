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

import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import groovy.lang.Closure;
import org.gradle.api.Action;

public interface ModelBackedDependencyAwareComponentMixIn<T extends ComponentDependencies> extends DependencyAwareComponent<T> {
	@Override
	@SuppressWarnings("unchecked")
	default T getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as((Class<T>) new TypeToken<T>(getClass()) {}.getRawType()).get();
	}

	@Override
	@SuppressWarnings("unchecked")
	default void dependencies(Action<? super T> action) {
		ModelProperties.getProperty(this, "dependencies").as((Class<T>) new TypeToken<T>(getClass()) {}.getRawType()).configure(action);
	}

	@Override
	@SuppressWarnings("unchecked")
	default void dependencies(@SuppressWarnings("rawtypes") Closure closure) {
		ModelProperties.getProperty(this, "dependencies").as((Class<T>) new TypeToken<T>(getClass()) {}.getRawType()).configure(closure);
	}
}
