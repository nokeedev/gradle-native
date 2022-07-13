/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import static dev.nokee.model.internal.buffers.ModelBuffers.typeOf;

interface DependencyBucketMixIn extends DependencyBucket, ModelBackedNamedMixIn {
	default void addDependency(Object notation) {
		ModelNodes.of(this).setComponent(ModelNodes.of(this).getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation)));
	}

	default void addDependency(Object notation, Action<? super ModuleDependency> action) {
		ModelNodes.of(this).setComponent(ModelNodes.of(this).getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation, action)));
	}

	@Override
	default Configuration getAsConfiguration() {
		return ModelNodeUtils.get(ModelNodes.of(this), Configuration.class);
	}
}
